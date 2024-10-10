<#if packageName??>
package ${packageName};
</#if>

<#if futurizedVerticle>
import io.vertx.core.VerticleBase;
<#else>
import io.vertx.core.AbstractVerticle;
</#if>

public class ${className} extends ${futurizedVerticle?then('VerticleBase', 'AbstractVerticle')} {

<#if futurizedVerticle>
    @Override
    public Future<?> start() {
        return super.start();
    }
<#else>
    @Override
    public void start() {

    }
</#if>

}
