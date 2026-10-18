// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dossier_sections_overview_facets.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DossierSectionsOverviewFacetsCWProxy {
  DossierSectionsOverviewFacets byEvidenceClass(
    Map<String, int>? byEvidenceClass,
  );

  DossierSectionsOverviewFacets bySource(Map<String, int>? bySource);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsOverviewFacets(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsOverviewFacets(...).copyWith(id: 12, name: "My name")
  /// ```
  DossierSectionsOverviewFacets call({
    Map<String, int>? byEvidenceClass,
    Map<String, int>? bySource,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDossierSectionsOverviewFacets.copyWith(...)` or call `instanceOfDossierSectionsOverviewFacets.copyWith.fieldName(value)` for a single field.
class _$DossierSectionsOverviewFacetsCWProxyImpl
    implements _$DossierSectionsOverviewFacetsCWProxy {
  const _$DossierSectionsOverviewFacetsCWProxyImpl(this._value);

  final DossierSectionsOverviewFacets _value;

  @override
  DossierSectionsOverviewFacets byEvidenceClass(
    Map<String, int>? byEvidenceClass,
  ) => call(byEvidenceClass: byEvidenceClass);

  @override
  DossierSectionsOverviewFacets bySource(Map<String, int>? bySource) =>
      call(bySource: bySource);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsOverviewFacets(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsOverviewFacets(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DossierSectionsOverviewFacets call({
    Object? byEvidenceClass = const $CopyWithPlaceholder(),
    Object? bySource = const $CopyWithPlaceholder(),
  }) {
    return DossierSectionsOverviewFacets(
      byEvidenceClass: byEvidenceClass == const $CopyWithPlaceholder()
          ? _value.byEvidenceClass
          // ignore: cast_nullable_to_non_nullable
          : byEvidenceClass as Map<String, int>?,
      bySource: bySource == const $CopyWithPlaceholder()
          ? _value.bySource
          // ignore: cast_nullable_to_non_nullable
          : bySource as Map<String, int>?,
    );
  }
}

extension $DossierSectionsOverviewFacetsCopyWith
    on DossierSectionsOverviewFacets {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDossierSectionsOverviewFacets.copyWith(...)` or `instanceOfDossierSectionsOverviewFacets.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DossierSectionsOverviewFacetsCWProxy get copyWith =>
      _$DossierSectionsOverviewFacetsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DossierSectionsOverviewFacets _$DossierSectionsOverviewFacetsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DossierSectionsOverviewFacets', json, ($checkedConvert) {
  final val = DossierSectionsOverviewFacets(
    byEvidenceClass: $checkedConvert(
      'byEvidenceClass',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, (e as num).toInt()),
      ),
    ),
    bySource: $checkedConvert(
      'bySource',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, (e as num).toInt()),
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$DossierSectionsOverviewFacetsToJson(
  DossierSectionsOverviewFacets instance,
) => <String, dynamic>{
  'byEvidenceClass': ?instance.byEvidenceClass,
  'bySource': ?instance.bySource,
};
