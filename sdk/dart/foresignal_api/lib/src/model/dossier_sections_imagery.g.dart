// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dossier_sections_imagery.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DossierSectionsImageryCWProxy {
  DossierSectionsImagery status(DossierSectionsImageryStatusEnum status);

  DossierSectionsImagery message(String? message);

  DossierSectionsImagery links(DossierSectionsOverviewLinks? links);

  DossierSectionsImagery facets(DossierSectionsOverviewFacets? facets);

  DossierSectionsImagery truncated(bool? truncated);

  DossierSectionsImagery items(List<DossierSectionsImageryItemsInner>? items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsImagery(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsImagery(...).copyWith(id: 12, name: "My name")
  /// ```
  DossierSectionsImagery call({
    DossierSectionsImageryStatusEnum status,
    String? message,
    DossierSectionsOverviewLinks? links,
    DossierSectionsOverviewFacets? facets,
    bool? truncated,
    List<DossierSectionsImageryItemsInner>? items,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDossierSectionsImagery.copyWith(...)` or call `instanceOfDossierSectionsImagery.copyWith.fieldName(value)` for a single field.
class _$DossierSectionsImageryCWProxyImpl
    implements _$DossierSectionsImageryCWProxy {
  const _$DossierSectionsImageryCWProxyImpl(this._value);

  final DossierSectionsImagery _value;

  @override
  DossierSectionsImagery status(DossierSectionsImageryStatusEnum status) =>
      call(status: status);

  @override
  DossierSectionsImagery message(String? message) => call(message: message);

  @override
  DossierSectionsImagery links(DossierSectionsOverviewLinks? links) =>
      call(links: links);

  @override
  DossierSectionsImagery facets(DossierSectionsOverviewFacets? facets) =>
      call(facets: facets);

  @override
  DossierSectionsImagery truncated(bool? truncated) =>
      call(truncated: truncated);

  @override
  DossierSectionsImagery items(List<DossierSectionsImageryItemsInner>? items) =>
      call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsImagery(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsImagery(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DossierSectionsImagery call({
    Object? status = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? facets = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
  }) {
    return DossierSectionsImagery(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as DossierSectionsImageryStatusEnum,
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
          : items as List<DossierSectionsImageryItemsInner>?,
    );
  }
}

extension $DossierSectionsImageryCopyWith on DossierSectionsImagery {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDossierSectionsImagery.copyWith(...)` or `instanceOfDossierSectionsImagery.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DossierSectionsImageryCWProxy get copyWith =>
      _$DossierSectionsImageryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DossierSectionsImagery _$DossierSectionsImageryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DossierSectionsImagery', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['status']);
  final val = DossierSectionsImagery(
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$DossierSectionsImageryStatusEnumEnumMap, v),
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
            (e) => DossierSectionsImageryItemsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$DossierSectionsImageryToJson(
  DossierSectionsImagery instance,
) => <String, dynamic>{
  'status': _$DossierSectionsImageryStatusEnumEnumMap[instance.status]!,
  'message': ?instance.message,
  'links': ?instance.links?.toJson(),
  'facets': ?instance.facets?.toJson(),
  'truncated': ?instance.truncated,
  'items': ?instance.items?.map((e) => e.toJson()).toList(),
};

const _$DossierSectionsImageryStatusEnumEnumMap = {
  DossierSectionsImageryStatusEnum.ok: 'ok',
  DossierSectionsImageryStatusEnum.empty: 'empty',
  DossierSectionsImageryStatusEnum.unavailable: 'unavailable',
  DossierSectionsImageryStatusEnum.notInstalled: 'not-installed',
};
