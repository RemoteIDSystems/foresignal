// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dossier_sections_related.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DossierSectionsRelatedCWProxy {
  DossierSectionsRelated status(DossierSectionsRelatedStatusEnum status);

  DossierSectionsRelated message(String? message);

  DossierSectionsRelated links(DossierSectionsOverviewLinks? links);

  DossierSectionsRelated facets(DossierSectionsOverviewFacets? facets);

  DossierSectionsRelated truncated(bool? truncated);

  DossierSectionsRelated nearby(List<RelatedObject>? nearby);

  DossierSectionsRelated operational(List<RelatedObject>? operational);

  DossierSectionsRelated infrastructure(List<RelatedObject>? infrastructure);

  DossierSectionsRelated historical(List<RelatedObject>? historical);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsRelated(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsRelated(...).copyWith(id: 12, name: "My name")
  /// ```
  DossierSectionsRelated call({
    DossierSectionsRelatedStatusEnum status,
    String? message,
    DossierSectionsOverviewLinks? links,
    DossierSectionsOverviewFacets? facets,
    bool? truncated,
    List<RelatedObject>? nearby,
    List<RelatedObject>? operational,
    List<RelatedObject>? infrastructure,
    List<RelatedObject>? historical,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDossierSectionsRelated.copyWith(...)` or call `instanceOfDossierSectionsRelated.copyWith.fieldName(value)` for a single field.
class _$DossierSectionsRelatedCWProxyImpl
    implements _$DossierSectionsRelatedCWProxy {
  const _$DossierSectionsRelatedCWProxyImpl(this._value);

  final DossierSectionsRelated _value;

  @override
  DossierSectionsRelated status(DossierSectionsRelatedStatusEnum status) =>
      call(status: status);

  @override
  DossierSectionsRelated message(String? message) => call(message: message);

  @override
  DossierSectionsRelated links(DossierSectionsOverviewLinks? links) =>
      call(links: links);

  @override
  DossierSectionsRelated facets(DossierSectionsOverviewFacets? facets) =>
      call(facets: facets);

  @override
  DossierSectionsRelated truncated(bool? truncated) =>
      call(truncated: truncated);

  @override
  DossierSectionsRelated nearby(List<RelatedObject>? nearby) =>
      call(nearby: nearby);

  @override
  DossierSectionsRelated operational(List<RelatedObject>? operational) =>
      call(operational: operational);

  @override
  DossierSectionsRelated infrastructure(List<RelatedObject>? infrastructure) =>
      call(infrastructure: infrastructure);

  @override
  DossierSectionsRelated historical(List<RelatedObject>? historical) =>
      call(historical: historical);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsRelated(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsRelated(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DossierSectionsRelated call({
    Object? status = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? facets = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
    Object? nearby = const $CopyWithPlaceholder(),
    Object? operational = const $CopyWithPlaceholder(),
    Object? infrastructure = const $CopyWithPlaceholder(),
    Object? historical = const $CopyWithPlaceholder(),
  }) {
    return DossierSectionsRelated(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as DossierSectionsRelatedStatusEnum,
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
      nearby: nearby == const $CopyWithPlaceholder()
          ? _value.nearby
          // ignore: cast_nullable_to_non_nullable
          : nearby as List<RelatedObject>?,
      operational: operational == const $CopyWithPlaceholder()
          ? _value.operational
          // ignore: cast_nullable_to_non_nullable
          : operational as List<RelatedObject>?,
      infrastructure: infrastructure == const $CopyWithPlaceholder()
          ? _value.infrastructure
          // ignore: cast_nullable_to_non_nullable
          : infrastructure as List<RelatedObject>?,
      historical: historical == const $CopyWithPlaceholder()
          ? _value.historical
          // ignore: cast_nullable_to_non_nullable
          : historical as List<RelatedObject>?,
    );
  }
}

extension $DossierSectionsRelatedCopyWith on DossierSectionsRelated {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDossierSectionsRelated.copyWith(...)` or `instanceOfDossierSectionsRelated.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DossierSectionsRelatedCWProxy get copyWith =>
      _$DossierSectionsRelatedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DossierSectionsRelated _$DossierSectionsRelatedFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DossierSectionsRelated', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['status']);
  final val = DossierSectionsRelated(
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$DossierSectionsRelatedStatusEnumEnumMap, v),
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
    nearby: $checkedConvert(
      'nearby',
      (v) => (v as List<dynamic>?)
          ?.map((e) => RelatedObject.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    operational: $checkedConvert(
      'operational',
      (v) => (v as List<dynamic>?)
          ?.map((e) => RelatedObject.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    infrastructure: $checkedConvert(
      'infrastructure',
      (v) => (v as List<dynamic>?)
          ?.map((e) => RelatedObject.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    historical: $checkedConvert(
      'historical',
      (v) => (v as List<dynamic>?)
          ?.map((e) => RelatedObject.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$DossierSectionsRelatedToJson(
  DossierSectionsRelated instance,
) => <String, dynamic>{
  'status': _$DossierSectionsRelatedStatusEnumEnumMap[instance.status]!,
  'message': ?instance.message,
  'links': ?instance.links?.toJson(),
  'facets': ?instance.facets?.toJson(),
  'truncated': ?instance.truncated,
  'nearby': ?instance.nearby?.map((e) => e.toJson()).toList(),
  'operational': ?instance.operational?.map((e) => e.toJson()).toList(),
  'infrastructure': ?instance.infrastructure?.map((e) => e.toJson()).toList(),
  'historical': ?instance.historical?.map((e) => e.toJson()).toList(),
};

const _$DossierSectionsRelatedStatusEnumEnumMap = {
  DossierSectionsRelatedStatusEnum.ok: 'ok',
  DossierSectionsRelatedStatusEnum.empty: 'empty',
  DossierSectionsRelatedStatusEnum.unavailable: 'unavailable',
  DossierSectionsRelatedStatusEnum.notInstalled: 'not-installed',
};
