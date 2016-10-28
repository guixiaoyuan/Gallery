/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


#pragma version(1)
#pragma rs java_package_name(com.tct.gallery3d.polaroid)

#pragma rs_fp_relaxed

const int BLEND_MODE_NORMAL       = 1;
const int BLEND_MODE_MULTIPLY     = 2;
const int BLEND_MODE_SCREEN       = 3;
const int BLEND_MODE_SOFTLIGHT    = 4;
const int BLEND_MODE_LINEAR_DODGE = 5;
const int BLEND_MODE_LINEAR_BURN  = 6;



static rs_allocation gTexture;
static rs_sampler gSampler;
static float2 gCoordMul;
static int gBlendMode = BLEND_MODE_NORMAL;
static float gOpacity = 1.0f;

static const float4 fPointFive = { 0.5f, 0.5f, 0.5f, 0.5f };
static const float4 fOne = { 1.0f, 1.0f, 1.0f, 1.0f };
static const float4 fTwo = { 2.0f, 2.0f, 2.0f, 2.0f };

void setBlendMode(int mode){
    gBlendMode = mode;
}

void setOpacity(float opacity){
    gOpacity = opacity;
}

void setTexture(rs_allocation texture, rs_sampler sampler){
    gTexture = texture;
    gSampler = sampler;
}

void setOutputResolution(int outWidth, int outHeight){
    gCoordMul.x = 1.0f / ((float) (outWidth));
    gCoordMul.y = 1.0f / ((float) (outHeight));
}

uchar4  __attribute__((kernel)) root(uchar4 iIn, uint32_t x, uint32_t y) {
    float2 coords = { (float) x, (float) y };
    coords *= gCoordMul;
    float4 fTexel = rsSample(gTexture, gSampler, coords);
    
    if (fTexel.a > 0.0f){
        fTexel.rgb = clamp(fTexel.rgb / fTexel.a, 0.0f, 1.0f);
    }
    //const float4 fTexel = convert_float4(iTexel) / 255.0f;
    const float4 fIn = convert_float4(iIn) / 255.0f;
    
    float alpha = fTexel.a * gOpacity;
    if (alpha <= 0.01f){
        return iIn;
    }
    
    float4 fTmpOut;
    if (gBlendMode == BLEND_MODE_NORMAL){
        fTmpOut = fTexel;
    }
    else if (gBlendMode == BLEND_MODE_MULTIPLY){
        fTmpOut = fIn * fTexel;
    }
    else if (gBlendMode == BLEND_MODE_SOFTLIGHT){
        // Using Illusions.ru formula, as it is much more simple that current's Photoshop formula
        // Illusions.ru: pow(a, pow(2, 2 * (0.5f - b))
        fTmpOut = native_powr(fIn, native_powr(fTwo, fOne - 2.0f * fTexel));
    }
    else if (gBlendMode == BLEND_MODE_SCREEN){
        fTmpOut = fOne - (fOne - fTexel) * (fOne - fIn);
    }
    else if (gBlendMode == BLEND_MODE_LINEAR_DODGE){
        fTmpOut = clamp(fIn + fTexel, 0.0f, 1.0f);
    }
    else if (gBlendMode == BLEND_MODE_LINEAR_BURN){
        fTexel += 0.0001f; // Avoid division by zero...
        fTmpOut = clamp(fOne - (fOne - fIn) / fTexel, 0.0f, 1.0f);
    }
    
    float4 fOut = fIn * (1.0f - alpha) + fTmpOut * alpha;
    
    uchar4 iOut = convert_uchar4(255.0f * fOut);
    iOut.a = 0xFF;
    
    /*
    if (y == 100 && fTexel.a != 0.0f && fTexel.a != 1.0f){
        rsDebug("========== gBlendMode", gBlendMode);
        rsDebug("iIn", iIn);
        rsDebug("fIn", fIn);
        rsDebug("gCoordMul", gCoordMul);
        rsDebug("coords", coords);
        rsDebug("fTexel", fTexel);
        rsDebug("fTmpOut", fTmpOut);
        rsDebug("alpha", alpha);
        rsDebug("fOut", fOut);
        rsDebug("iOut", iOut);
    }
    */
    
    return iOut;
}

