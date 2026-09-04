const fs = require('fs');
const path = require('path');
const mkdirp = require('mkdirp');
const spawn = require("child_process").spawn;
const getFafDependencies = require('js-sdk/scripts/utils/getFafDependencies');
const {getFafModuleDir} = require('js-sdk/scripts/utils/fafModuleUtils');
const {replacePathToFafModulesInAllPackageJsonFiles} = require('js-sdk/scripts/utils/patchPackageJsonUtils');

const auditDir = process.env.BUILD_AUDIT_DIR;
const yarnAuditCwd = `${auditDir}/${path.basename(process.cwd())}`;

const CVE_REPORT_NAME = process.env.CVE_REPORT_NAME;
const CVE_DEV_REPORT_NAME = process.env.CVE_DEV_REPORT_NAME;
const CVE_REPORT_FULL_NAME = process.env.CVE_REPORT_FULL_NAME;
const CVE_DEV_REPORT_FULL_NAME = process.env.CVE_DEV_REPORT_FULL_NAME;
const CVE_IGNORE_LIST_NAME = process.env.CVE_IGNORE_LIST_NAME;
const CVE_GROUP_DEPENDENCIES = "dependencies";
const CVE_GROUP_DEV_DEPENDENCIES = "devDependencies";

const YARN_MIN_VERSION = "1.16";

const yarnСmd = /^win/.test(process.platform) ? "yarn.cmd" : "yarn";

const compareVersions = function (a, b) {
    var i, diff;
    var regExStrip0 = /(\.0+)+$/;
    var segmentsA = a.replace(regExStrip0, '').split('.');
    var segmentsB = b.replace(regExStrip0, '').split('.');
    var l = Math.min(segmentsA.length, segmentsB.length);

    for (i = 0; i < l; i++) {
        diff = parseInt(segmentsA[i], 10) - parseInt(segmentsB[i], 10);
        if (diff) {
            return diff;
        }
    }
    return segmentsA.length - segmentsB.length;
};


function readIgnoreListFileContent() {
    let ignoreListPath = `./${CVE_IGNORE_LIST_NAME}`;

    if (!fs.existsSync(ignoreListPath)) {
        console.log(`[Error]: Can not find file: ${ignoreListPath}`);
        process.exit(1);
    }
    const ignoreListFileContent = fs.readFileSync(ignoreListPath, {encoding: 'utf8'});

    return JSON.parse(ignoreListFileContent);
}

function generateFullReport(data, ignoreList) {
    const reportObject = {
        reports: [],
        summary: {
            data: {
                ignoredVulnerabilities: {
                    info: 0,
                    low: 0,
                    moderate: 0,
                    high: 0,
                    critical: 0
                }
            }
        }
    };
    return data.trim().split(/\n/).reduce((memo, line) => {
        let lineJson;
        try {
            lineJson = JSON.parse(line);
        }catch (e) {
            return memo;
        }

        if (lineJson.type === "auditAdvisory") {
            if (ignoreList.includes(lineJson.data.advisory.module_name)) {
                lineJson.ignore = true;
                memo.summary.data.ignoredVulnerabilities[lineJson.data.advisory.severity]++;
            }
            memo.reports.push(lineJson);
        }

        if (lineJson.type === "auditSummary") {
            lineJson.data.ignoredVulnerabilities = memo.summary.data.ignoredVulnerabilities;
            memo.summary = lineJson;
        }

        return memo;
    }, reportObject);

}

function generateNewIssuesReport(fullReport) {
    return fullReport.reports.filter(report => {
        return !report.ignore;
    });
}

function logNewIssuesReport(newReports) {

    const textResult = newReports.map(report => {

        return `
          Severity: ${report.data.advisory.severity}
          Title: ${report.data.advisory.title}
          Package: ${report.data.advisory.module_name}
          Patched in: ${report.data.advisory.patched_versions}
          Path: ${report.data.resolution.path}
          More info: ${report.data.advisory.url}
          `;
    }).join("\r\n");

    console.log(textResult);
}

function writeReportToFile(report, fileName) {
    const dir = path.dirname(fileName);
    mkdirp.sync(dir);
    fs.writeFileSync(fileName, JSON.stringify(report, null, 2));
}


function runAudit(options) {
    const {auditGroup, ignoreList, fullReportFileName, newIssuesReportFileName} = options;

    return new Promise((resolve, reject) => {
        const s = spawn(yarnСmd, [
            "audit",
            "--groups",
            auditGroup,
            "--json"
        ], {cwd: yarnAuditCwd});

        let stdout = '';
        s.stdout.on('data', (data) => {
            stdout += String(data);
        });

        let error = ''
        s.stderr.on('data', (data) => {
            error += String(data);
        });

        s.on('close', (code) => {
            if (code === 1) {
                console.log("Got an error while executing 'yarn audit':");
                console.error(error, '\n');
            } else {
                const fullReport = generateFullReport(stdout, ignoreList);
                const newIssuesReport = generateNewIssuesReport(fullReport);

                if (newIssuesReport.length) {
                    const type = auditGroup === CVE_GROUP_DEPENDENCIES ? "runtime" : "dev";
                    console.log(`${newIssuesReport.length} new ${type} CVE detected, check ${newIssuesReportFileName}`);
                    logNewIssuesReport(newIssuesReport);
                    writeReportToFile(newIssuesReport, newIssuesReportFileName);
                }

                writeReportToFile(fullReport, fullReportFileName);
            }

            resolve();
        });
    });
}

function checkYarnVersion() {
    return new Promise((resolve, reject) => {
        const s = spawn(yarnСmd, ["-v"]);

        let version = '';
        s.stdout.on('data', (data) => {
            version = String(data);
        });

        let error = ''
        s.stderr.on('data', (data) => {
            error += String(data);
        });

        s.on('close', (code) => {
            if (code !== 0) {
                console.error("Got an error while executing 'yarn -v':");
                console.error(error, '\n');
            }

            if (compareVersions(version.replace(/[\r\n]+/g, ''), YARN_MIN_VERSION) < 0) {
                reject(`Yarn should be upgraded to ${YARN_MIN_VERSION} or higher.`);
            }

            resolve();
        })
    })
}

// Make a copy of all faf modules in the build folder
// in order to be able to modify package.json without any concerns
function copyFafModules(fafDependencies) {
    const cwd = process.cwd();

    fafDependencies.map((fafModule) => {
        const sourcePath = getFafModuleDir(fafModule, cwd);
        const destPath = `${auditDir}/node_modules/${fafModule}`;
        return {sourcePath, destPath};
    }).concat([{
        sourcePath: cwd,
        destPath: yarnAuditCwd
    }]).map(({sourcePath, destPath}) => {
        mkdirp.sync(destPath);
        fs.copyFileSync(`${sourcePath}/package.json`, `${destPath}/package.json`);
        fs.copyFileSync(`${sourcePath}/yarn.lock`, `${destPath}/yarn.lock`);
    });
}

function auditDependencies() {
    const {ignoreList, ignoreDevList} = readIgnoreListFileContent();

    const runtimeAuditOptions = {
        auditGroup: CVE_GROUP_DEPENDENCIES,
        ignoreList: ignoreList,
        fullReportFileName: CVE_REPORT_FULL_NAME,
        newIssuesReportFileName: CVE_REPORT_NAME
    };
    const devAuditOptions = {
        auditGroup: CVE_GROUP_DEV_DEPENDENCIES,
        ignoreList: ignoreDevList,
        fullReportFileName: CVE_DEV_REPORT_FULL_NAME,
        newIssuesReportFileName: CVE_DEV_REPORT_NAME
    };

    return Promise.all([
        runAudit(runtimeAuditOptions),
        runAudit(devAuditOptions)
    ]).then(() => {
        console.log("CVE audit finished.");
    });
}


(async function () {
    try {
        await checkYarnVersion();
        const fafDependencies = getFafDependencies();
        copyFafModules(fafDependencies);
        await replacePathToFafModulesInAllPackageJsonFiles(fafDependencies, yarnAuditCwd);
        await auditDependencies();
    } catch (error) {
        console.error(error);
        process.exit(1);
    }
})()


