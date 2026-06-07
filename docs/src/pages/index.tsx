import type {ReactNode} from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import Heading from '@theme/Heading';
import HomepageFeatures from '@site/src/components/HomepageFeatures';

import styles from './index.module.css';

const modrinthUrl = 'https://modrinth.com/plugin/evenmorefish';
const discordUrl = 'https://discord.gg/9fRbqWTnHS';
const jenkinsUrl = 'https://ci.codemc.io/job/EvenMoreFish/job/EvenMoreFish/';
const docsUrl = '/docs/intro';

const whyUseIt = [
  'Start with 70+ fish out of the box, then shape the experience around your server.',
  'Run competitions, rewards, shops, baits, and progression without stitching together multiple plugins.',
  'Control catches by biome, region, rarity, commands, and economy integration.',
];

const marketStats = {
  modrinthDownloads: 23054,
  modrinthFollowers: 94,
  spigotDownloads: 62423,
  spigotRating: 4.6,
  spigotRatings: 136,
  activeServers: 1436,
  activePlayers: 6275,
  playersRecord: 8186,
};

const totalDownloads =
  marketStats.modrinthDownloads + marketStats.spigotDownloads;

const installSteps = [
  'Download the latest release from Modrinth.',
  'Drop the .jar into your server\'s plugins folder.',
  'Restart the server, then edit the generated config files.',
];

const popularDocs = [
  {
    title: 'Installation',
    description: 'Set up the plugin and get your first server running quickly.',
    to: '/docs/intro',
  },
  {
    title: 'Commands',
    description: 'Find player and admin commands for fish, bait, shops, and more.',
    to: '/docs/commands',
  },
  {
    title: 'Configuration',
    description: 'Customize fish, rarities, regions, rewards, and gameplay rules.',
    to: '/docs/category/configuration',
  },
  {
    title: 'Competitions',
    description: 'Configure events, schedules, bossbars, and leaderboards.',
    to: '/docs/features/competitions/types',
  },
];

const usefulLinks = [
  {
    title: 'FAQ',
    description: 'Answers for admin commands, fish rewards, bait, shop values, and common setup questions.',
    to: '/docs/support/faq',
  },
  {
    title: 'Downloads',
    description: 'Stable Modrinth releases, dev builds on Jenkins, and legacy Spigot downloads.',
    to: '/docs/downloads',
  },
  {
    title: 'Support',
    description: 'Join Discord for help with configuration, bugs, migrations, and integrations.',
    href: discordUrl,
  },
  {
    title: 'GitHub',
    description: 'Browse the codebase, open issues, and track changes across releases.',
    href: 'https://github.com/EvenMoreFish/EvenMoreFish',
  },
];

function formatNumber(value: number): string {
  return new Intl.NumberFormat('en-US').format(value);
}

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();

  return (
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
      <div className={clsx('container', styles.heroContainer)}>
        <div className={styles.heroCopy}>
          <Heading as="h1" className={styles.heroTitle}>
            {siteConfig.title}
          </Heading>
          <p className={styles.heroSubtitle}>
            A feature-rich fishing plugin for custom fish, competitions, baits,
            shops, rewards, and server-specific progression.
          </p>
          <p className={styles.heroDescription}>
            Build a fishing system that fits your server: create collectible
            catches, run timed events, reward players, restrict fish by biome or
            region, and connect the whole experience to your economy and
            PlaceholderAPI setup.
          </p>
          <div className={styles.heroActions}>
            <Link
              className={clsx('button button--lg', styles.modrinthButton)}
              href={modrinthUrl}>
              Download on Modrinth
            </Link>
            <Link
              className={clsx('button button--lg', styles.docsButton)}
              to={docsUrl}>
              Read installation guide
            </Link>
            <Link
              className={clsx('button button--lg', styles.discordButton)}
              href={discordUrl}>
              Join Discord
            </Link>
          </div>
          <div className={styles.marketPanel}>
            <div className={styles.marketSummary}>
              <strong>{formatNumber(totalDownloads)} downloads</strong>
              <p>
                {formatNumber(marketStats.modrinthDownloads)} on Modrinth and{' '}
                {formatNumber(marketStats.spigotDownloads)} on Spigot.
              </p>
            </div>
            <div className={styles.marketStatsGrid}>
              <div className={styles.marketStat}>
                <span className={styles.marketStatLabel}>Active servers</span>
                <strong>{formatNumber(marketStats.activeServers)}</strong>
              </div>
              <div className={styles.marketStat}>
                <span className={styles.marketStatLabel}>Active players</span>
                <strong>{formatNumber(marketStats.activePlayers)}</strong>
                <p>Peak in sampled history: {formatNumber(marketStats.playersRecord)}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}

function SectionHeading({
  title,
  subtitle,
}: {
  title: string;
  subtitle: string;
}) {
  return (
    <div className={styles.sectionHeading}>
      <Heading as="h2">{title}</Heading>
      <p>{subtitle}</p>
    </div>
  );
}

export default function Home(): ReactNode {
  const {siteConfig} = useDocusaurusContext();

  return (
    <Layout
      title={`${siteConfig.title}`}
      description="EvenMoreFish is a Minecraft fishing plugin with custom fish, competitions, baits, shops, rewards, and extensive server customization.">
      <HomepageHeader />
      <main className={styles.emfHome}>
        <section className={styles.section}>
          <div className="container">
            <SectionHeading
              title="Why use EvenMoreFish?"
              subtitle="Custom fish, competitions, baits, economy tools, and server-side control in one plugin."
            />
            <div className={styles.infoGrid}>
              {whyUseIt.map((item) => (
                <div key={item} className={styles.infoCard}>
                  <p>{item}</p>
                </div>
              ))}
            </div>
          </div>
        </section>

        <HomepageFeatures />

        <section className={styles.section} id="install">
          <div className="container">
            <SectionHeading
              title="Install in 60 seconds"
              subtitle="Download it, drop it into your plugins folder, restart, and start configuring."
            />
            <div className={styles.stepsGrid}>
              {installSteps.map((step, index) => (
                <div key={step} className={styles.stepCard}>
                  <span className={styles.stepNumber}>0{index + 1}</span>
                  <p>{step}</p>
                </div>
              ))}
            </div>
            <div className={clsx(styles.sectionActions, styles.installActions)}>
              <Link className={clsx('button', styles.docsButton)} to={docsUrl}>
                Open installation guide
              </Link>
              <Link className={clsx('button', styles.modrinthButton)} href={modrinthUrl}>
                Go to Modrinth
              </Link>
            </div>
          </div>
        </section>

        <section className={styles.section}>
          <div className="container">
            <SectionHeading
              title="Popular docs"
              subtitle="Start with setup, commands, configuration, and competition docs."
            />
            <div className={styles.linkGrid}>
              {popularDocs.map((doc) => (
                <Link key={doc.title} className={styles.linkCard} to={doc.to}>
                  <Heading as="h3">{doc.title}</Heading>
                  <p>{doc.description}</p>
                </Link>
              ))}
            </div>
          </div>
        </section>

        <section className={styles.section}>
          <div className="container">
            <SectionHeading
              title="FAQ & useful links"
              subtitle="The fastest way to get answers, downloads, and support."
            />
            <div className={styles.linkGrid}>
              {usefulLinks.map((item) =>
                item.to ? (
                  <Link key={item.title} className={styles.linkCard} to={item.to}>
                    <Heading as="h3">{item.title}</Heading>
                    <p>{item.description}</p>
                  </Link>
                ) : (
                  <Link key={item.title} className={styles.linkCard} href={item.href}>
                    <Heading as="h3">{item.title}</Heading>
                    <p>{item.description}</p>
                  </Link>
                ),
              )}
            </div>
          </div>
        </section>

        <section className={clsx(styles.section, styles.finalSection)}>
          <div className="container">
            <div className={styles.ctaPanel}>
              <div>
                <Heading as="h2">Ready to add fishing progression to your server?</Heading>
                <p>
                  Start with the stable Modrinth release, use the docs for setup,
                  and join Discord if you need help configuring competitions,
                  regions, rewards, or integrations.
                </p>
              </div>
              <div className={styles.sectionActions}>
                <Link
                  className={clsx('button button--lg', styles.modrinthButton)}
                  href={modrinthUrl}>
                  Download now
                </Link>
                <Link
                  className={clsx('button button--lg', styles.discordButton)}
                  href={discordUrl}>
                  Join Discord
                </Link>
                <Link
                  className={clsx('button button--lg', styles.docsButton)}
                  href={jenkinsUrl}>
                  Dev Builds
                </Link>
              </div>
            </div>
          </div>
        </section>
      </main>
    </Layout>
  );
}
