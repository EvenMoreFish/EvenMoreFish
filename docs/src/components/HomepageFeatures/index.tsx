import type {ReactNode} from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  description: ReactNode;
};

const FeatureList: FeatureItem[] = [
    {
        title: 'Custom Fish & Items',
        Svg: require('@site/static/img/home/fish.svg').default, 
        description: (
            <>
                Create unique fish with custom items, lore, lengths, and effects. Supports custom heads and model data.
            </>
        ),
    },
    {
        title: 'Dynamic Competitions',
        Svg: require('@site/static/img/home/leaderboard-star.svg').default,
        description: (
            <>
                6 competition types with configurable rewards, bossbars, and leaderboards.
            </>
        ),
    },
    {
        title: 'Advanced Customization',
        Svg: require('@site/static/img/home/settings.svg').default,
        description: (
            <>
                Biome-specific fish, region restrictions, commands on catch, and more.
            </>
        ),
    },
    {
        title: 'Fish Shop & Economy',
        Svg: require('@site/static/img/home/coins-swap.svg').default,
        description: (
            <>
                Players can sell their catches with secure transactions and configurable prices.
            </>
        ),
    },
    {
        title: 'Bait System',
        Svg: require('@site/static/img/home/bait.svg').default,
        description: (
            <>
                Special baits that affect catch rates for specific fish or rarities.
            </>
        ),
    },
    {
        title: 'PlaceholderAPI Integration',
        Svg: require('@site/static/img/home/placeholder.svg').default,
        description: (
            <>
                Display competition standings and timers anywhere with comprehensive placeholders.
            </>
        ),
    },
];

function Feature({title, Svg, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4', styles.featureItem)}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): ReactNode {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className={clsx("row", styles.homepageRow)}>
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
