import React from 'react';
import clsx from 'clsx';

interface EmfVersionBadgeProps {
    frontMatter: {
        version?: string;
        experimental?: string;
        [key: string]: unknown;
    };
}

export default function EmfVersionBadge({ frontMatter }: EmfVersionBadgeProps) {
    const hasVersion = Boolean(frontMatter?.version);
    const isExperimental = Boolean(frontMatter?.experimental);

    // Return null if neither badge should be shown
    if (!hasVersion && !isExperimental) return null;

    const versionBadge = hasVersion ? (
        <span className={clsx('emf-version-badge', 'badge', `badge--${getVariantFromVersion(frontMatter.version!)}`)}>
            Version: {frontMatter.version}
        </span>
    ) : null;

    const experimentalBadge = isExperimental ? (
        <span className={clsx('emf-experimental-badge', 'badge', 'badge--warning')}>
            Warning: EXPERIMENTAL
        </span>
    ) : null;

    return (
        <div className="emf-badge-container" style={{ display: 'flex', gap: '0.5rem' }}>
            {versionBadge}
            {experimentalBadge}
        </div>
    );
}

function getVariantFromVersion(version: string): string {
    const channel = version.split('-')[1]; //test this, 2.0.0 (-RC, -BETA, -ALPHA etc) just RC atm, RC = orange
    switch (channel) {
        case 'RC':
            return 'warning';
        default:
            return 'secondary';
    }
}