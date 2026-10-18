// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'collection_gap_recommendations_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CollectionGapRecommendationsInnerCWProxy {
  CollectionGapRecommendationsInner type(
    CollectionGapRecommendationsInnerTypeEnum type,
  );

  CollectionGapRecommendationsInner sourceId(String? sourceId);

  CollectionGapRecommendationsInner sensorFamily(String? sensorFamily);

  CollectionGapRecommendationsInner text(String text);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollectionGapRecommendationsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollectionGapRecommendationsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  CollectionGapRecommendationsInner call({
    CollectionGapRecommendationsInnerTypeEnum type,
    String? sourceId,
    String? sensorFamily,
    String text,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCollectionGapRecommendationsInner.copyWith(...)` or call `instanceOfCollectionGapRecommendationsInner.copyWith.fieldName(value)` for a single field.
class _$CollectionGapRecommendationsInnerCWProxyImpl
    implements _$CollectionGapRecommendationsInnerCWProxy {
  const _$CollectionGapRecommendationsInnerCWProxyImpl(this._value);

  final CollectionGapRecommendationsInner _value;

  @override
  CollectionGapRecommendationsInner type(
    CollectionGapRecommendationsInnerTypeEnum type,
  ) => call(type: type);

  @override
  CollectionGapRecommendationsInner sourceId(String? sourceId) =>
      call(sourceId: sourceId);

  @override
  CollectionGapRecommendationsInner sensorFamily(String? sensorFamily) =>
      call(sensorFamily: sensorFamily);

  @override
  CollectionGapRecommendationsInner text(String text) => call(text: text);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollectionGapRecommendationsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollectionGapRecommendationsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CollectionGapRecommendationsInner call({
    Object? type = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? sensorFamily = const $CopyWithPlaceholder(),
    Object? text = const $CopyWithPlaceholder(),
  }) {
    return CollectionGapRecommendationsInner(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as CollectionGapRecommendationsInnerTypeEnum,
      sourceId: sourceId == const $CopyWithPlaceholder()
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String?,
      sensorFamily: sensorFamily == const $CopyWithPlaceholder()
          ? _value.sensorFamily
          // ignore: cast_nullable_to_non_nullable
          : sensorFamily as String?,
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
    );
  }
}

extension $CollectionGapRecommendationsInnerCopyWith
    on CollectionGapRecommendationsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCollectionGapRecommendationsInner.copyWith(...)` or `instanceOfCollectionGapRecommendationsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CollectionGapRecommendationsInnerCWProxy get copyWith =>
      _$CollectionGapRecommendationsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CollectionGapRecommendationsInner _$CollectionGapRecommendationsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CollectionGapRecommendationsInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['type', 'text']);
  final val = CollectionGapRecommendationsInner(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$CollectionGapRecommendationsInnerTypeEnumEnumMap, v),
    ),
    sourceId: $checkedConvert('sourceId', (v) => v as String?),
    sensorFamily: $checkedConvert('sensorFamily', (v) => v as String?),
    text: $checkedConvert('text', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$CollectionGapRecommendationsInnerToJson(
  CollectionGapRecommendationsInner instance,
) => <String, dynamic>{
  'type': _$CollectionGapRecommendationsInnerTypeEnumEnumMap[instance.type]!,
  'sourceId': ?instance.sourceId,
  'sensorFamily': ?instance.sensorFamily,
  'text': instance.text,
};

const _$CollectionGapRecommendationsInnerTypeEnumEnumMap = {
  CollectionGapRecommendationsInnerTypeEnum.enableSource: 'enable-source',
  CollectionGapRecommendationsInnerTypeEnum.addReceiver: 'add-receiver',
  CollectionGapRecommendationsInnerTypeEnum.addSensor: 'add-sensor',
  CollectionGapRecommendationsInnerTypeEnum.none: 'none',
};
