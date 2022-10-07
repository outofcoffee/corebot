/**
 * @param github github client
 * @param context job context
 * @param assetPaths {Array<string>|string}
 * @returns {Promise<void>}
 */
module.exports = async ({github, context}) => {
    if (!context.ref.match(/refs\/tags\/.+/)) {
        console.warn(`Unsupported ref: ${context.ref}`);
        return;
    }
    const releaseVersion = context.ref.split('/')[2];
    if (!releaseVersion) {
        console.warn('No release version - aborting');
        return;
    }

    let releaseId = await getExistingRelease(releaseVersion, github);
    if (!releaseId) {
        releaseId = await createRelease(releaseVersion, github);
        console.log(`Release ID: ${releaseId}`);
    }
};

/**
 * @param releaseVersion
 * @param github
 * @returns {Promise<string|null>} the release ID, or `null`
 */
async function getExistingRelease(releaseVersion, github) {
    console.log(`Checking for release: ${releaseVersion}`);
    const releases = await github.rest.repos.listReleases({
        owner: 'outofcoffee',
        repo: 'corebot',
    });
    let releaseId;
    for (const release of releases.data) {
        if (release.tag_name === releaseVersion) {
            releaseId = release.id;
            console.log(`Found existing release with ID: ${releaseId}`);
            break;
        }
    }
    return releaseId;
}

/**
 * @param releaseVersion
 * @param github
 * @returns {Promise<string>} the release ID
 */
async function createRelease(releaseVersion, github) {
    console.log(`Creating release: ${releaseVersion}`);
    const release = await github.rest.repos.createRelease({
        owner: 'outofcoffee',
        repo: 'corebot',
        tag_name: releaseVersion,
        body: 'See Docker images on [Docker Hub](https://hub.docker.com/r/outofcoffee/corebot).\n\nSee [change log](https://github.com/outofcoffee/corebot/blob/master/CHANGELOG.md).',
    });
    return release.data.id;
}
