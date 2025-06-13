import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)
let discord = "https://discord.gg/9fRbqWTnHS";

const config: Config = {
  title: 'EvenMoreFish',
  tagline: 'Fish & Hunt Fish',
  favicon: 'img/brand/favicon.ico',

  // Set the production url of your site here
  url: 'https://evenmorefish.github.io',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: '/EvenMoreFish/',
  trailingSlash: false,
  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'evenmorefish',
  projectName: 'evenmorefish',

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },
  presets: [
    [
      'classic',
      {
        docs: {
        sidebarPath: './sidebars.ts',
        lastVersion: 'current',
        },
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],
  themeConfig: {
    image: 'img/brand/evenmorefish-social-card.png',
    navbar: {
      title: 'EvenMoreFish',
      logo: {
        alt: 'EvenMoreFish Logo',
        src: 'img/brand/favicon.ico',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'tutorialSidebar',
          position: 'left',
          label: 'Documentation',
        },
        {
          href: 'https://modrinth.com/plugin/evenmorefish',
          className: 'header-modrinth-link',
          'aria-label': 'Download (Modrinth)',
          // label: 'Download (Modrinth)',
          position: 'right',
        },
        {
          href: 'https://ci.codemc.io/job/EvenMoreFish/job/EvenMoreFish/',
          // label: 'Experimental Download (Jenkins)',
          className: 'header-jenkins-link',
          'aria-label': 'Experimental Download (Jenkins)',
          position: 'right',
        },
        {
          href: discord,
          // label: 'Discord',
          className: 'header-discord-link',
          'aria-label': 'Discord',
          position: 'right',
        },
        {
          href: 'https://github.com/EvenMoreFish/EvenMoreFish',
          // label: 'GitHub',
          className: 'header-github-link',
          'aria-label': 'Github',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Documentation',
              to: '/docs/intro',
            },
            {
              label: 'FAQs',
              to: '/docs/support/faq',
            }
          ],
        },
        {
          title: 'Support',
          items: [
            {
              label: 'Discord',
              href: discord,
            },
          ],
        },
        {
          title: 'Downloads',
          items: [
            {
              label: 'Modrinth',
              href: 'https://modrinth.com/plugin/evenmorefish',
            },
            {
              label: 'Spigot',
              href: 'https://www.spigotmc.org/resources/evenmorefish-%E2%96%AA-extensive-fishing-plugin-%E2%96%AA.91310/',
            }
          ]
        },
      ],
      copyright: `Copyright © 2021 - ${new Date().getFullYear()} EvenMoreFish`,
    },
    algolia: {
      appId: 'OYCYW3JATA',
      apiKey: '078a0c0e8ee1517d753bdccc6f56716a', //safe to commit

      indexName: 'evenmorefishio',

      // Optional: see doc section below
      contextualSearch: true,

      // Optional: Replace parts of the item URLs from Algolia. Useful when using the same search index for multiple deployments using a different baseUrl. You can use regexp or string in the `from` param. For example: localhost:3000 vs myCompany.com/docs
      replaceSearchResultPathname: {
        from: '/docs/', // or as RegExp: /\/docs\//
        to: '/',
      },

      searchParameters: {},
      searchPagePath: 'search',
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: ['java'],
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
