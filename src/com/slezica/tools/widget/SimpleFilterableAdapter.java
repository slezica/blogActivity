/*
 * Copyright (C) 2011 Santiago Lezica
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

package com.slezica.tools.widget;

import java.util.List;

import android.content.Context;

public class SimpleFilterableAdapter<ObjectType> extends FilterableAdapter<ObjectType, String> {
    
    public SimpleFilterableAdapter(Context context, int resourceId,    int textResourceId, List<ObjectType> objects) {
        super(context, resourceId, textResourceId, objects);
    }

    public SimpleFilterableAdapter(Context context, int resourceId,
            List<ObjectType> objects) {
        super(context, resourceId, objects);
    }

    public SimpleFilterableAdapter(Context context, int resourceId) {
        super(context, resourceId);
    }

    public SimpleFilterableAdapter(Context context, List<ObjectType> objects) {
        super(context, objects);
    }

    public SimpleFilterableAdapter(Context context) {
        super(context);
    }

    @Override
    protected String prepareFilter(CharSequence seq) {
        return seq.toString().toLowerCase();
    }

    @Override
    protected boolean passesFilter(ObjectType object, String constraint) {
        String repr = object.toString().toLowerCase();
        
        if (repr.startsWith(constraint))
            return true;
        
        else {
            final String[] words = repr.split(" ");
            final int wordCount = words.length;
            
            for (int i = 0; i < wordCount; i++) {
                if (words[i].startsWith(constraint))
                    return true;
            }
        }
        
        return false;
    }
}
