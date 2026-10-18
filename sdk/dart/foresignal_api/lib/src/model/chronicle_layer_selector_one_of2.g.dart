// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_layer_selector_one_of2.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleLayerSelectorOneOf2CWProxy {
  ChronicleLayerSelectorOneOf2 type(ChronicleLayerSelectorOneOf2TypeEnum type);

  ChronicleLayerSelectorOneOf2 watchlistId(String watchlistId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerSelectorOneOf2(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerSelectorOneOf2(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleLayerSelectorOneOf2 call({
    ChronicleLayerSelectorOneOf2TypeEnum type,
    String watchlistId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleLayerSelectorOneOf2.copyWith(...)` or call `instanceOfChronicleLayerSelectorOneOf2.copyWith.fieldName(value)` for a single field.
class _$ChronicleLayerSelectorOneOf2CWProxyImpl
    implements _$ChronicleLayerSelectorOneOf2CWProxy {
  const _$ChronicleLayerSelectorOneOf2CWProxyImpl(this._value);

  final ChronicleLayerSelectorOneOf2 _value;

  @override
  ChronicleLayerSelectorOneOf2 type(
    ChronicleLayerSelectorOneOf2TypeEnum type,
  ) => call(type: type);

  @override
  ChronicleLayerSelectorOneOf2 watchlistId(String watchlistId) =>
      call(watchlistId: watchlistId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerSelectorOneOf2(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerSelectorOneOf2(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleLayerSelectorOneOf2 call({
    Object? type = const $CopyWithPlaceholder(),
    Object? watchlistId = const $CopyWithPlaceholder(),
  }) {
    return ChronicleLayerSelectorOneOf2(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ChronicleLayerSelectorOneOf2TypeEnum,
      watchlistId:
          watchlistId == const $CopyWithPlaceholder() || watchlistId == null
          ? _value.watchlistId
          // ignore: cast_nullable_to_non_nullable
          : watchlistId as String,
    );
  }
}

extension $ChronicleLayerSelectorOneOf2CopyWith
    on ChronicleLayerSelectorOneOf2 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleLayerSelectorOneOf2.copyWith(...)` or `instanceOfChronicleLayerSelectorOneOf2.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleLayerSelectorOneOf2CWProxy get copyWith =>
      _$ChronicleLayerSelectorOneOf2CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleLayerSelectorOneOf2 _$ChronicleLayerSelectorOneOf2FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleLayerSelectorOneOf2', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type', 'watchlistId']);
  final val = ChronicleLayerSelectorOneOf2(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ChronicleLayerSelectorOneOf2TypeEnumEnumMap, v),
    ),
    watchlistId: $checkedConvert('watchlistId', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$ChronicleLayerSelectorOneOf2ToJson(
  ChronicleLayerSelectorOneOf2 instance,
) => <String, dynamic>{
  'type': _$ChronicleLayerSelectorOneOf2TypeEnumEnumMap[instance.type]!,
  'watchlistId': instance.watchlistId,
};

const _$ChronicleLayerSelectorOneOf2TypeEnumEnumMap = {
  ChronicleLayerSelectorOneOf2TypeEnum.watchlist: 'watchlist',
};
