# simple-web-crawler

## What is that?

Simple easy to understand Web Crawler Class written in Java

## How it works?

It starts from a given URL and traverses the whole website tree, during the traversing it
saves the website pages onto the desktop

## Features

* Traversing all pages on a website
* Calculating all and unique URLs on the website
* Ignoring URLs to external websites especially to social media websites
* Saving all pages (only HTML content without CSS or JS or other media like images) into a directory on the desktop
* Keeping the website hierarchy of the directories
* Easy to use and understand

## How to use it?

* Clone the repository or download the class from src and put it somewhere in your project folder
* Import the package
* Create an object by passing the wanted URL as an argument
```java
SimpleCrawler crawler = new SimpleCrawler("https://example.com");
```
* Invoke the crawl method on the object
```java
crawler.crawl();
```
* Wait until the Crawler finishes his work
* That's it!

## License

SimpleCrawler is licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html)
