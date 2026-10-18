// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dossier_sections_overview.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DossierSectionsOverviewCWProxy {
  DossierSectionsOverview status(DossierSectionsOverviewStatusEnum status);

  DossierSectionsOverview message(String? message);

  DossierSectionsOverview links(DossierSectionsOverviewLinks? links);

  DossierSectionsOverview facets(DossierSectionsOverviewFacets? facets);

  DossierSectionsOverview truncated(bool? truncated);

  DossierSectionsOverview fields(Map<String, Object>? fields);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsOverview(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsOverview(...).copyWith(id: 12, name: "My name")
  /// ```
  DossierSectionsOverview call({
    DossierSectionsOverviewStatusEnum status,
    String? message,
    DossierSectionsOverviewLinks? links,
    DossierSectionsOverviewFacets? facets,
    bool? truncated,
    Map<String, Object>? fields,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDossierSectionsOverview.copyWith(...)` or call `instanceOfDossierSectionsOverview.copyWith.fieldName(value)` for a single field.
class _$DossierSectionsOverviewCWProxyImpl
    implements _$DossierSectionsOverviewCWProxy {
  const _$DossierSectionsOverviewCWProxyImpl(this._value);

  final DossierSectionsOverview _value;

  @override
  DossierSectionsOverview status(DossierSectionsOverviewStatusEnum status) =>
      call(status: status);

  @override
  DossierSectionsOverview message(String? message) => call(message: message);

  @override
  DossierSectionsOverview links(DossierSectionsOverviewLinks? links) =>
      call(links: links);

  @override
  DossierSectionsOverview facets(DossierSectionsOverviewFacets? facets) =>
      call(facets: facets);

  @override
  DossierSectionsOverview truncated(bool? truncated) =>
      call(truncated: truncated);

  @override
  DossierSectionsOverview fields(Map<String, Object>? fields) =>
      call(fields: fields);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsOverview(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsOverview(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DossierSectionsOverview call({
    Object? status = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? facets = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
    Object? fields = const $CopyWithPlaceholder(),
  }) {
    return DossierSectionsOverview(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as DossierSectionsOverviewStatusEnum,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      links: links == const $CopyWithPlaceholder()
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as DossierSectionsOverviewLinks?,
      facets: facets == const $CopyWithPlaceholder()
          ? _value.facets
          // ignore: cast_nullable_to_non_nullable
          : facets as DossierSectionsOverviewFacets?,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
      fields: fields == const $CopyWithPlaceholder()
          ? _value.fields
          // ignore: cast_nullable_to_non_nullable
          : fields as Map<String, Object>?,
    );
  }
}

extension $DossierSectionsOverviewCopyWith on DossierSectionsOverview {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDossierSectionsOverview.copyWith(...)` or `instanceOfDossierSectionsOverview.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DossierSectionsOverviewCWProxy get copyWith =>
      _$DossierSectionsOverviewCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DossierSectionsOverview _$DossierSectionsOverviewFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DossierSectionsOverview', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['status']);
  final val = DossierSectionsOverview(
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$DossierSectionsOverviewStatusEnumEnumMap, v),
    ),
    message: $checkedConvert('message', (v) => v as String?),
    links: $checkedConvert(
      'links',
      (v) => v == null
          ? null
          : DossierSectionsOverviewLinks.fromJson(v as Map<String, dynamic>),
    ),
    facets: $checkedConvert(
      'facets',
      (v) => v == null
          ? null
          : DossierSectionsOverviewFacets.fromJson(v as Map<String, dynamic>),
    ),
    truncated: $checkedConvert('truncated', (v) => v as bool?),
    fields: $checkedConvert(
      'fields',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
  );
  return val;
});

Map<String, dynamic> _$DossierSectionsOverviewToJson(
  DossierSectionsOverview instance,
) => <String, dynamic>{
  'status': _$DossierSectionsOverviewStatusEnumEnumMap[instance.status]!,
  'message': ?instance.message,
  'links': ?instance.links?.toJson(),
  'facets': ?instance.facets?.toJson(),
  'truncated': ?instance.truncated,
  'fields': ?instance.fields,
};

const _$DossierSectionsOverviewStatusEnumEnumMap = {
  DossierSectionsOverviewStatusEnum.ok: 'ok',
  DossierSectionsOverviewStatusEnum.empty: 'empty',
  DossierSectionsOverviewStatusEnum.unavailable: 'unavailable',
  DossierSectionsOverviewStatusEnum.notInstalled: 'not-installed',
};
