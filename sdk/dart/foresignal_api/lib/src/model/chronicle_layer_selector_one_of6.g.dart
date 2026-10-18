// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_layer_selector_one_of6.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleLayerSelectorOneOf6CWProxy {
  ChronicleLayerSelectorOneOf6 type(ChronicleLayerSelectorOneOf6TypeEnum type);

  ChronicleLayerSelectorOneOf6 refs(List<String> refs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerSelectorOneOf6(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerSelectorOneOf6(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleLayerSelectorOneOf6 call({
    ChronicleLayerSelectorOneOf6TypeEnum type,
    List<String> refs,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleLayerSelectorOneOf6.copyWith(...)` or call `instanceOfChronicleLayerSelectorOneOf6.copyWith.fieldName(value)` for a single field.
class _$ChronicleLayerSelectorOneOf6CWProxyImpl
    implements _$ChronicleLayerSelectorOneOf6CWProxy {
  const _$ChronicleLayerSelectorOneOf6CWProxyImpl(this._value);

  final ChronicleLayerSelectorOneOf6 _value;

  @override
  ChronicleLayerSelectorOneOf6 type(
    ChronicleLayerSelectorOneOf6TypeEnum type,
  ) => call(type: type);

  @override
  ChronicleLayerSelectorOneOf6 refs(List<String> refs) => call(refs: refs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerSelectorOneOf6(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerSelectorOneOf6(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleLayerSelectorOneOf6 call({
    Object? type = const $CopyWithPlaceholder(),
    Object? refs = const $CopyWithPlaceholder(),
  }) {
    return ChronicleLayerSelectorOneOf6(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ChronicleLayerSelectorOneOf6TypeEnum,
      refs: refs == const $CopyWithPlaceholder() || refs == null
          ? _value.refs
          // ignore: cast_nullable_to_non_nullable
          : refs as List<String>,
    );
  }
}

extension $ChronicleLayerSelectorOneOf6CopyWith
    on ChronicleLayerSelectorOneOf6 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleLayerSelectorOneOf6.copyWith(...)` or `instanceOfChronicleLayerSelectorOneOf6.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleLayerSelectorOneOf6CWProxy get copyWith =>
      _$ChronicleLayerSelectorOneOf6CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleLayerSelectorOneOf6 _$ChronicleLayerSelectorOneOf6FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleLayerSelectorOneOf6', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type', 'refs']);
  final val = ChronicleLayerSelectorOneOf6(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ChronicleLayerSelectorOneOf6TypeEnumEnumMap, v),
    ),
    refs: $checkedConvert(
      'refs',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChronicleLayerSelectorOneOf6ToJson(
  ChronicleLayerSelectorOneOf6 instance,
) => <String, dynamic>{
  'type': _$ChronicleLayerSelectorOneOf6TypeEnumEnumMap[instance.type]!,
  'refs': instance.refs,
};

const _$ChronicleLayerSelectorOneOf6TypeEnumEnumMap = {
  ChronicleLayerSelectorOneOf6TypeEnum.series: 'series',
};
