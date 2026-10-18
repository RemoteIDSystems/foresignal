// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_layer_selector_one_of7.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleLayerSelectorOneOf7CWProxy {
  ChronicleLayerSelectorOneOf7 type(ChronicleLayerSelectorOneOf7TypeEnum type);

  ChronicleLayerSelectorOneOf7 productIds(List<String> productIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerSelectorOneOf7(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerSelectorOneOf7(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleLayerSelectorOneOf7 call({
    ChronicleLayerSelectorOneOf7TypeEnum type,
    List<String> productIds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleLayerSelectorOneOf7.copyWith(...)` or call `instanceOfChronicleLayerSelectorOneOf7.copyWith.fieldName(value)` for a single field.
class _$ChronicleLayerSelectorOneOf7CWProxyImpl
    implements _$ChronicleLayerSelectorOneOf7CWProxy {
  const _$ChronicleLayerSelectorOneOf7CWProxyImpl(this._value);

  final ChronicleLayerSelectorOneOf7 _value;

  @override
  ChronicleLayerSelectorOneOf7 type(
    ChronicleLayerSelectorOneOf7TypeEnum type,
  ) => call(type: type);

  @override
  ChronicleLayerSelectorOneOf7 productIds(List<String> productIds) =>
      call(productIds: productIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerSelectorOneOf7(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerSelectorOneOf7(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleLayerSelectorOneOf7 call({
    Object? type = const $CopyWithPlaceholder(),
    Object? productIds = const $CopyWithPlaceholder(),
  }) {
    return ChronicleLayerSelectorOneOf7(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ChronicleLayerSelectorOneOf7TypeEnum,
      productIds:
          productIds == const $CopyWithPlaceholder() || productIds == null
          ? _value.productIds
          // ignore: cast_nullable_to_non_nullable
          : productIds as List<String>,
    );
  }
}

extension $ChronicleLayerSelectorOneOf7CopyWith
    on ChronicleLayerSelectorOneOf7 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleLayerSelectorOneOf7.copyWith(...)` or `instanceOfChronicleLayerSelectorOneOf7.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleLayerSelectorOneOf7CWProxy get copyWith =>
      _$ChronicleLayerSelectorOneOf7CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleLayerSelectorOneOf7 _$ChronicleLayerSelectorOneOf7FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleLayerSelectorOneOf7', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type', 'productIds']);
  final val = ChronicleLayerSelectorOneOf7(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ChronicleLayerSelectorOneOf7TypeEnumEnumMap, v),
    ),
    productIds: $checkedConvert(
      'productIds',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChronicleLayerSelectorOneOf7ToJson(
  ChronicleLayerSelectorOneOf7 instance,
) => <String, dynamic>{
  'type': _$ChronicleLayerSelectorOneOf7TypeEnumEnumMap[instance.type]!,
  'productIds': instance.productIds,
};

const _$ChronicleLayerSelectorOneOf7TypeEnumEnumMap = {
  ChronicleLayerSelectorOneOf7TypeEnum.rasterProducts: 'raster-products',
};
