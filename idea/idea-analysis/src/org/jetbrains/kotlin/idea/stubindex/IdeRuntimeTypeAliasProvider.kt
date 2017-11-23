/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.stubindex

import org.jetbrains.kotlin.analyzer.ModuleInfo
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.TypeAliasDescriptor
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.idea.caches.resolve.LibraryInfo
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.lazy.RuntimeTypeAliasesProvider
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter

class IdeRuntimeTypeAliasProvider : RuntimeTypeAliasesProvider {
    override fun findTypeAliases(
            moduleDescriptor: ModuleDescriptor,
            packages: Collection<FqName>
    ): Collection<TypeAliasDescriptor> {
        val result = mutableListOf<TypeAliasDescriptor>()

        for (dependencyModuleDescriptor in moduleDescriptor.allDependencyModules) {
            if (dependencyModuleDescriptor !is ModuleDescriptorImpl) continue
            if (dependencyModuleDescriptor.getCapability(ModuleInfo.Capability) !is LibraryInfo) continue

            for (packageFqName in packages) {
                dependencyModuleDescriptor.packageFragmentProviderForContent.getPackageFragments(packageFqName)
                        .flatMapTo(result) {
                            packageFragmentDescriptor ->
                            packageFragmentDescriptor.getMemberScope()
                                    .getContributedDescriptors(DescriptorKindFilter.TYPE_ALIASES)
                                    .filterIsInstance<TypeAliasDescriptor>()
                }
            }
        }

        return result
    }
}
