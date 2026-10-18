// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dossier_sections_impacts.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DossierSectionsImpactsCWProxy {
  DossierSectionsImpacts status(DossierSectionsImpactsStatusEnum status);

  DossierSectionsImpacts message(String? message);

  DossierSectionsImpacts links(DossierSectionsOverviewLinks? links);

  DossierSectionsImpacts facets(DossierSectionsOverviewFacets? facets);

  DossierSectionsImpacts truncated(bool? truncated);

  DossierSectionsImpacts items(List<DossierSectionsImpactsItemsInner>? items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsImpacts(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsImpacts(...).copyWith(id: 12, name: "My name")
  /// ```
  DossierSectionsImpacts call({
    DossierSectionsImpactsStatusEnum status,
    String? message,
    DossierSectionsOverviewLinks? links,
    DossierSectionsOverviewFacets? facets,
    bool? truncated,
    List<DossierSectionsImpactsItemsInner>? items,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDossierSectionsImpacts.copyWith(...)` or call `instanceOfDossierSectionsImpacts.copyWith.fieldName(value)` for a single field.
class _$DossierSectionsImpactsCWProxyImpl
    implements _$DossierSectionsImpactsCWProxy {
  const _$DossierSectionsImpactsCWProxyImpl(this._value);

  final DossierSectionsImpacts _value;

  @override
  DossierSectionsImpacts status(DossierSectionsImpactsStatusEnum status) =>
      call(status: status);

  @override
  DossierSectionsImpacts message(String? message) => call(message: message);

  @override
  DossierSectionsImpacts links(DossierSectionsOverviewLinks? links) =>
      call(links: links);

  @override
  DossierSectionsImpacts facets(DossierSectionsOverviewFacets? facets) =>
      call(facets: facets);

  @override
  DossierSectionsImpacts truncated(bool? truncated) =>
      call(truncated: truncated);

  @override
  DossierSectionsImpacts items(List<DossierSectionsImpactsItemsInner>? items) =>
      call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsImpacts(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsImpacts(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DossierSectionsImpacts call({
    Object? status = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? facets = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
  }) {
    return DossierSectionsImpacts(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as DossierSectionsImpactsStatusEnum,
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
      items: items == const $CopyWithPlaceholder()
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<DossierSectionsImpactsItemsInner>?,
    );
  }
}

extension $DossierSectionsImpactsCopyWith on DossierSectionsImpacts {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDossierSectionsImpacts.copyWith(...)` or `instanceOfDossierSectionsImpacts.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DossierSectionsImpactsCWProxy get copyWith =>
      _$DossierSectionsImpactsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DossierSectionsImpacts _$DossierSectionsImpactsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DossierSectionsImpacts', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['status']);
  final val = DossierSectionsImpacts(
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$DossierSectionsImpactsStatusEnumEnumMap, v),
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
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => DossierSectionsImpactsItemsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$DossierSectionsImpactsToJson(
  DossierSectionsImpacts instance,
) => <String, dynamic>{
  'status': _$DossierSectionsImpactsStatusEnumEnumMap[instance.status]!,
  'message': ?instance.message,
  'links': ?instance.links?.toJson(),
  'facets': ?instance.facets?.toJson(),
  'truncated': ?instance.truncated,
  'items': ?instance.items?.map((e) => e.toJson()).toList(),
};

const _$DossierSectionsImpactsStatusEnumEnumMap = {
  DossierSectionsImpactsStatusEnum.ok: 'ok',
  DossierSectionsImpactsStatusEnum.empty: 'empty',
  DossierSectionsImpactsStatusEnum.unavailable: 'unavailable',
  DossierSectionsImpactsStatusEnum.notInstalled: 'not-installed',
};
