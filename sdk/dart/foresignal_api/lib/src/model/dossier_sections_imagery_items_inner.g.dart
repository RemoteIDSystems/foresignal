// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dossier_sections_imagery_items_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DossierSectionsImageryItemsInnerCWProxy {
  DossierSectionsImageryItemsInner ref(String ref);

  DossierSectionsImageryItemsInner kind(
    DossierSectionsImageryItemsInnerKindEnum kind,
  );

  DossierSectionsImageryItemsInner title(String? title);

  DossierSectionsImageryItemsInner time(DateTime? time);

  DossierSectionsImageryItemsInner sourceId(String? sourceId);

  DossierSectionsImageryItemsInner frameUrl(String? frameUrl);

  DossierSectionsImageryItemsInner thumbnailUrl(String? thumbnailUrl);

  DossierSectionsImageryItemsInner bbox(List<num>? bbox);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsImageryItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsImageryItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  DossierSectionsImageryItemsInner call({
    String ref,
    DossierSectionsImageryItemsInnerKindEnum kind,
    String? title,
    DateTime? time,
    String? sourceId,
    String? frameUrl,
    String? thumbnailUrl,
    List<num>? bbox,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDossierSectionsImageryItemsInner.copyWith(...)` or call `instanceOfDossierSectionsImageryItemsInner.copyWith.fieldName(value)` for a single field.
class _$DossierSectionsImageryItemsInnerCWProxyImpl
    implements _$DossierSectionsImageryItemsInnerCWProxy {
  const _$DossierSectionsImageryItemsInnerCWProxyImpl(this._value);

  final DossierSectionsImageryItemsInner _value;

  @override
  DossierSectionsImageryItemsInner ref(String ref) => call(ref: ref);

  @override
  DossierSectionsImageryItemsInner kind(
    DossierSectionsImageryItemsInnerKindEnum kind,
  ) => call(kind: kind);

  @override
  DossierSectionsImageryItemsInner title(String? title) => call(title: title);

  @override
  DossierSectionsImageryItemsInner time(DateTime? time) => call(time: time);

  @override
  DossierSectionsImageryItemsInner sourceId(String? sourceId) =>
      call(sourceId: sourceId);

  @override
  DossierSectionsImageryItemsInner frameUrl(String? frameUrl) =>
      call(frameUrl: frameUrl);

  @override
  DossierSectionsImageryItemsInner thumbnailUrl(String? thumbnailUrl) =>
      call(thumbnailUrl: thumbnailUrl);

  @override
  DossierSectionsImageryItemsInner bbox(List<num>? bbox) => call(bbox: bbox);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierSectionsImageryItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierSectionsImageryItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DossierSectionsImageryItemsInner call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? time = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? frameUrl = const $CopyWithPlaceholder(),
    Object? thumbnailUrl = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
  }) {
    return DossierSectionsImageryItemsInner(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as DossierSectionsImageryItemsInnerKindEnum,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      time: time == const $CopyWithPlaceholder()
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as DateTime?,
      sourceId: sourceId == const $CopyWithPlaceholder()
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String?,
      frameUrl: frameUrl == const $CopyWithPlaceholder()
          ? _value.frameUrl
          // ignore: cast_nullable_to_non_nullable
          : frameUrl as String?,
      thumbnailUrl: thumbnailUrl == const $CopyWithPlaceholder()
          ? _value.thumbnailUrl
          // ignore: cast_nullable_to_non_nullable
          : thumbnailUrl as String?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
    );
  }
}

extension $DossierSectionsImageryItemsInnerCopyWith
    on DossierSectionsImageryItemsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDossierSectionsImageryItemsInner.copyWith(...)` or `instanceOfDossierSectionsImageryItemsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DossierSectionsImageryItemsInnerCWProxy get copyWith =>
      _$DossierSectionsImageryItemsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DossierSectionsImageryItemsInner _$DossierSectionsImageryItemsInnerFromJson(
  Map<String, dynamic> json,
) =>
    $checkedCreate('DossierSectionsImageryItemsInner', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['ref', 'kind']);
      final val = DossierSectionsImageryItemsInner(
        ref: $checkedConvert('ref', (v) => v as String),
        kind: $checkedConvert(
          'kind',
          (v) =>
              $enumDecode(_$DossierSectionsImageryItemsInnerKindEnumEnumMap, v),
        ),
        title: $checkedConvert('title', (v) => v as String?),
        time: $checkedConvert(
          'time',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        sourceId: $checkedConvert('sourceId', (v) => v as String?),
        frameUrl: $checkedConvert('frameUrl', (v) => v as String?),
        thumbnailUrl: $checkedConvert('thumbnailUrl', (v) => v as String?),
        bbox: $checkedConvert(
          'bbox',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$DossierSectionsImageryItemsInnerToJson(
  DossierSectionsImageryItemsInner instance,
) => <String, dynamic>{
  'ref': instance.ref,
  'kind': _$DossierSectionsImageryItemsInnerKindEnumEnumMap[instance.kind]!,
  'title': ?instance.title,
  'time': ?instance.time?.toIso8601String(),
  'sourceId': ?instance.sourceId,
  'frameUrl': ?instance.frameUrl,
  'thumbnailUrl': ?instance.thumbnailUrl,
  'bbox': ?instance.bbox,
};

const _$DossierSectionsImageryItemsInnerKindEnumEnumMap = {
  DossierSectionsImageryItemsInnerKindEnum.rasterFrame: 'raster-frame',
  DossierSectionsImageryItemsInnerKindEnum.satdumpProduct: 'satdump-product',
  DossierSectionsImageryItemsInnerKindEnum.stacScene: 'stac-scene',
};
