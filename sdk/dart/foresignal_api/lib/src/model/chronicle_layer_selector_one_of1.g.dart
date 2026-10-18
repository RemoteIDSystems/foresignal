// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_layer_selector_one_of1.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleLayerSelectorOneOf1CWProxy {
  ChronicleLayerSelectorOneOf1 type(ChronicleLayerSelectorOneOf1TypeEnum type);

  ChronicleLayerSelectorOneOf1 viewId(String viewId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerSelectorOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerSelectorOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleLayerSelectorOneOf1 call({
    ChronicleLayerSelectorOneOf1TypeEnum type,
    String viewId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleLayerSelectorOneOf1.copyWith(...)` or call `instanceOfChronicleLayerSelectorOneOf1.copyWith.fieldName(value)` for a single field.
class _$ChronicleLayerSelectorOneOf1CWProxyImpl
    implements _$ChronicleLayerSelectorOneOf1CWProxy {
  const _$ChronicleLayerSelectorOneOf1CWProxyImpl(this._value);

  final ChronicleLayerSelectorOneOf1 _value;

  @override
  ChronicleLayerSelectorOneOf1 type(
    ChronicleLayerSelectorOneOf1TypeEnum type,
  ) => call(type: type);

  @override
  ChronicleLayerSelectorOneOf1 viewId(String viewId) => call(viewId: viewId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerSelectorOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerSelectorOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleLayerSelectorOneOf1 call({
    Object? type = const $CopyWithPlaceholder(),
    Object? viewId = const $CopyWithPlaceholder(),
  }) {
    return ChronicleLayerSelectorOneOf1(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ChronicleLayerSelectorOneOf1TypeEnum,
      viewId: viewId == const $CopyWithPlaceholder() || viewId == null
          ? _value.viewId
          // ignore: cast_nullable_to_non_nullable
          : viewId as String,
    );
  }
}

extension $ChronicleLayerSelectorOneOf1CopyWith
    on ChronicleLayerSelectorOneOf1 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleLayerSelectorOneOf1.copyWith(...)` or `instanceOfChronicleLayerSelectorOneOf1.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleLayerSelectorOneOf1CWProxy get copyWith =>
      _$ChronicleLayerSelectorOneOf1CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleLayerSelectorOneOf1 _$ChronicleLayerSelectorOneOf1FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleLayerSelectorOneOf1', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type', 'viewId']);
  final val = ChronicleLayerSelectorOneOf1(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ChronicleLayerSelectorOneOf1TypeEnumEnumMap, v),
    ),
    viewId: $checkedConvert('viewId', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$ChronicleLayerSelectorOneOf1ToJson(
  ChronicleLayerSelectorOneOf1 instance,
) => <String, dynamic>{
  'type': _$ChronicleLayerSelectorOneOf1TypeEnumEnumMap[instance.type]!,
  'viewId': instance.viewId,
};

const _$ChronicleLayerSelectorOneOf1TypeEnumEnumMap = {
  ChronicleLayerSelectorOneOf1TypeEnum.savedView: 'saved-view',
};
