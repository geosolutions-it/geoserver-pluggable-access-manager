<?xml version="1.0" encoding="UTF-8"?>
<sld:StyledLayerDescriptor xmlns="http://www.opengis.net/sld" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml" version="1.0.0">
    <sld:UserLayer>
        <sld:LayerFeatureConstraints>
            <sld:FeatureTypeConstraint/>
        </sld:LayerFeatureConstraints>
        <sld:UserStyle>
            <sld:Name>clc2000_L3_100m</sld:Name>
            <sld:Title/>
            <sld:FeatureTypeStyle>
                <sld:Name>name</sld:Name>
                <sld:FeatureTypeName>Feature</sld:FeatureTypeName>
                <sld:Rule>
                    <sld:RasterSymbolizer>
                        <sld:Geometry>
                            <ogc:PropertyName>grid</ogc:PropertyName>
                        </sld:Geometry>
                        <sld:ColorMap type="values">
                            <sld:ColorMapEntry color="#C6C6E0" opacity="1.0" quantity="1.0"/>
                            <sld:ColorMapEntry color="#000073" opacity="1.0" quantity="2.0"/>
                            <sld:ColorMapEntry color="#000073" opacity="1.0" quantity="3.0"/>
                            <sld:ColorMapEntry color="#0064D2" opacity="1.0" quantity="4.0"/>
                            <sld:ColorMapEntry color="#0064D2" opacity="1.0" quantity="5.0"/>
                            <sld:ColorMapEntry color="#5AB7DB" opacity="1.0" quantity="6.0"/>
                            <sld:ColorMapEntry color="#5AB7DB" opacity="1.0" quantity="7.0"/>
                            <sld:ColorMapEntry color="#00FFFF" opacity="1.0" quantity="8.0"/>
                            <sld:ColorMapEntry color="#00FFFF" opacity="1.0" quantity="9.0"/>
                            <sld:ColorMapEntry color="#28FE64" opacity="1.0" quantity="10.0"/>
                            <sld:ColorMapEntry color="#28FE64" opacity="1.0" quantity="11.0"/>
                            <sld:ColorMapEntry color="#508323" opacity="1.0" quantity="12.0"/>
                            <sld:ColorMapEntry color="#508323" opacity="1.0" quantity="13.0"/>
                        </sld:ColorMap>
                    </sld:RasterSymbolizer>
                </sld:Rule>
            </sld:FeatureTypeStyle>
        </sld:UserStyle>
    </sld:UserLayer>
</sld:StyledLayerDescriptor>