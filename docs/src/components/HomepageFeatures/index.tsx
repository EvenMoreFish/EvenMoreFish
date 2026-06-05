import type {ReactNode} from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  description: ReactNode;
};

const primaryFeatures: FeatureItem[] = [
  {
    title: 'Custom Fish & Rewards',
    Svg: require('@site/static/img/home/fish.svg').default,
    description: (
      <>
        Create collectible fish with custom items, rarities, lore, lengths,
        and rewards that fit your server theme.
      </>
    ),
  },
  {
    title: 'Competitions & Leaderboards',
    Svg: require('@site/static/img/home/leaderboard-star.svg').default,
    description: (
      <>
        Run timed fishing events with bossbars, schedules, rewards, and live
        standings that keep players competing.
      </>
    ),
  },
  {
    title: 'Economy, Baits & Progression',
    Svg: require('@site/static/img/home/coins-swap.svg').default,
    description: (
      <>
        Turn fishing into progression with shop values, targeted baits, and
        reward loops that give catches real value.
      </>
    ),
  },
];

const secondaryFeatures: FeatureItem[] = [
  {
    title: 'Biome & Region Rules',
    Svg: require('@site/static/img/home/settings.svg').default,
    description: (
      <>
        Restrict catches by biome, region, rarity, commands, and other
        server-specific rules.
      </>
    ),
  },
  {
    title: 'PlaceholderAPI',
    Svg: require('@site/static/img/home/placeholder.svg').default,
    description: (
      <>
        Expose standings, timers, and plugin data anywhere on your server with
        PlaceholderAPI support.
      </>
    ),
  },
  {
    title: 'Deep Configuration',
    Svg: require('@site/static/img/home/bait.svg').default,
    description: (
      <>
        Control rewards, fish behavior, progression, balancing, and admin
        workflows from config files.
      </>
    ),
  },
];

function PrimaryFeature({title, Svg, description}: FeatureItem) {
  return (
    <article className={styles.primaryCard}>
      <div className={styles.primaryIcon}>
        <Svg className={styles.primarySvg} role="img" />
      </div>
      <div className={styles.primaryContent}>
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </article>
  );
}

function SecondaryFeature({title, Svg, description}: FeatureItem) {
  return (
    <article className={styles.secondaryCard}>
      <div className={styles.secondaryIcon}>
        <Svg className={styles.secondarySvg} role="img" />
      </div>
      <div className={styles.secondaryContent}>
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </article>
  );
}

export default function HomepageFeatures(): ReactNode {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className={styles.sectionHeading}>
          <Heading as="h2">Core features</Heading>
          <p>
            Custom catches, competitions, economy tools, and progression systems
            built for server owners.
          </p>
        </div>

        <div className={styles.primaryGrid}>
          {primaryFeatures.map((feature) => (
            <PrimaryFeature key={feature.title} {...feature} />
          ))}
        </div>

        <div className={styles.secondaryGrid}>
          {secondaryFeatures.map((feature) => (
            <SecondaryFeature key={feature.title} {...feature} />
          ))}
        </div>
      </div>
    </section>
  );
}
