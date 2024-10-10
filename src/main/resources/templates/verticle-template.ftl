<#if packageName??>
package ${packageName};
</#if>

<#if futurizedVerticle>
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
<#else>
import io.vertx.core.AbstractVerticle;
</#if>

public class ${className} extends ${futurizedVerticle?then('VerticleBase', 'AbstractVerticle')} {

<#if futurizedVerticle>
    @Override
    public Future<?> start() throws Exception {
        return super.start();
    }
<#else>
    @Override
    public void start() {

    }
</#if>

}
