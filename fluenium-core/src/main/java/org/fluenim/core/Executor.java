package org.fluenim.core;

import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author Artur
 */
public class Executor {

    WebDriver driver;

    public class PageFollower {

        Executor then() {
            return Executor.this;
        }

        Executor then(final String whatAbout) {
            return Executor.this;
        }
    }

    public class Follower extends PageFollower {

        private final String xpath;

        public Follower(String xpath) {
            this.xpath = xpath;
        }

        Action so() {
            Action action = new Action();
            action.setXPath(xpath);
            return action;
        }
    }

    class Timed<T> {

        private int timeout = 10;

        T after(int timeout) {
            this.timeout = timeout;
            return (T) this;
        }

        protected void until(Function f) {
            (new WebDriverWait(driver, timeout)).until(f);
        }
    }

    class PageChecker extends Timed<PageChecker> {

        public PageFollower titleStartsWith(final String title) {
            until((ExpectedCondition<Boolean>) (WebDriver d) -> d.getTitle().startsWith(title));
            return new PageFollower();
        }
    }

    interface ActionBase {

        void setXPath(String xpath);
    }

    public class ElementChecker extends Timed<ElementChecker> implements ActionBase {

        private String xpath;

        public class AttributeMatcher {
            final String attributeName;
            public AttributeMatcher(String attributeName) {
                this.attributeName = attributeName;
            }
            ElementChecker matches(final String regexp) {
                until((ExpectedCondition<Boolean>) (WebDriver d)
                        -> {
                    String text = d.findElement(By.xpath(xpath)).getAttribute(attributeName);
                    return text != null ? d.findElement(By.xpath(xpath)).getText().matches(regexp) : regexp == text;
                }
                );
                return ElementChecker.this;
            }
            ElementChecker extsts(final String regexp) {
                until((ExpectedCondition<Boolean>) (WebDriver d)
                        -> {
                    String text = d.findElement(By.xpath(xpath)).getAttribute(attributeName);
                    return text != null;
                }
                );
                return ElementChecker.this;
            }
        }
        
        public class TextMatcher {
            public TextMatcher() {
            }
            ElementChecker matches(final String regexp) {
                until((ExpectedCondition<Boolean>) (WebDriver d)
                        -> {
                    String text = d.findElement(By.xpath(xpath)).getText();
                    return text != null ? d.findElement(By.xpath(xpath)).getText().matches(regexp) : regexp == text;
                }
                );
                return ElementChecker.this;
            }
            ElementChecker extsts(final String regexp) {
                until((ExpectedCondition<Boolean>) (WebDriver d)
                        -> {
                    String text = d.findElement(By.xpath(xpath)).getText();
                    return text != null;
                }
                );
                return ElementChecker.this;
            }
        }

        @Override
        public void setXPath(String xpath) {
            this.xpath = xpath;
        }

        public Follower isDisplayed() {
            until((ExpectedCondition<Boolean>) (WebDriver d)
                    -> d.findElement(By.xpath(this.xpath)).isDisplayed()
            );
            return new Follower(xpath);
        }

        /**
         * Check if element text matches the given regular expression
         *
         * @return
         */
        public TextMatcher text() {
            return new TextMatcher();
        }

        /**
         * Check if attribute text matches the given regular expression
         *
         * @param attributeName
         * @return
         */
        public AttributeMatcher attribute(final String attributeName) {
            return new AttributeMatcher(attributeName);
        }
    }

    public class Action implements ActionBase {

        private String xpath;

        @Override
        public void setXPath(String xpath) {
            this.xpath = xpath;
        }

        public Follower sendKeys(CharSequence... keys) {
            driver.findElement(By.xpath(this.xpath)).sendKeys(keys);
            return new Follower(xpath);
        }

        public Follower clickIt() {
            driver.findElement(By.xpath(this.xpath)).click();
            return new Follower(xpath);
        }
    }

    public class XPathSelector<T extends ActionBase> {

        private final T finalAction;
        private final String tagName;

        public class XPathElementSelector {

            private final String attribute;
            private String attributeValue;

            private String what() {
                return "//" + tagName + "[@" + attribute + "='" + attributeValue + "']";
            }

            public XPathElementSelector(String attribute) {
                this.attribute = attribute;
            }

            public T equalTo(final String value) {
                this.attributeValue = value;
                finalAction.setXPath(what());
                return finalAction;
            }
        }

        public XPathSelector(String tagName, T action) {
            this.tagName = tagName;
            this.finalAction = action;
        }

        public XPathElementSelector with(final String attribute) {
            return new XPathElementSelector(attribute);
        }
    }

    public class TagSelector<T extends ActionBase> {

        private final T action;

        public TagSelector(T action) {
            this.action = action;
        }

        public XPathSelector<T> tag(final String elementType) {
            return new XPathSelector<T>(elementType, action);
        }
    }

    private Executor(WebDriver driver) {
        this.driver = driver;
    }

    public TagSelector<ElementChecker> verifyThatElement() {
        return new TagSelector(new ElementChecker());
    }

    public ElementChecker verifyThatElement(final String xpath) {
        ElementChecker checker = new ElementChecker();
        checker.setXPath(xpath);
        return checker;
    }

    public PageChecker verifyThatPage() {
        return new PageChecker();
    }

    public TagSelector<Action> performOn() {
        return new TagSelector(new Action());
    }

    public Action performOn(final String xpath) {
        Action action = new Action();
        action.setXPath(xpath);
        return action;
    }

    PageChecker page() {
        return new PageChecker();
    }

    public static Executor using(WebDriver driver) {
        return new Executor(driver);
    }
}
