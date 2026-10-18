// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_layer_selector_one_of4.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleLayerSelectorOneOf4CWProxy {
  ChronicleLayerSelectorOneOf4 type(ChronicleLayerSelectorOneOf4TypeEnum type);

  ChronicleLayerSelectorOneOf4 situationIds(List<String> situationIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerSelectorOneOf4(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerSelectorOneOf4(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleLayerSelectorOneOf4 call({
    ChronicleLayerSelectorOneOf4TypeEnum type,
    List<String> situationIds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleLayerSelectorOneOf4.copyWith(...)` or call `instanceOfChronicleLayerSelectorOneOf4.copyWith.fieldName(value)` for a single field.
class _$ChronicleLayerSelectorOneOf4CWProxyImpl
    implements _$ChronicleLayerSelectorOneOf4CWProxy {
  const _$ChronicleLayerSelectorOneOf4CWProxyImpl(this._value);

  final ChronicleLayerSelectorOneOf4 _value;

  @override
  ChronicleLayerSelectorOneOf4 type(
    ChronicleLayerSelectorOneOf4TypeEnum type,
  ) => call(type: type);

  @override
  ChronicleLayerSelectorOneOf4 situationIds(List<String> situationIds) =>
      call(situationIds: situationIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerSelectorOneOf4(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerSelectorOneOf4(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleLayerSelectorOneOf4 call({
    Object? type = const $CopyWithPlaceholder(),
    Object? situationIds = const $CopyWithPlaceholder(),
  }) {
    return ChronicleLayerSelectorOneOf4(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ChronicleLayerSelectorOneOf4TypeEnum,
      situationIds:
          situationIds == const $CopyWithPlaceholder() || situationIds == null
          ? _value.situationIds
          // ignore: cast_nullable_to_non_nullable
          : situationIds as List<String>,
    );
  }
}

extension $ChronicleLayerSelectorOneOf4CopyWith
    on ChronicleLayerSelectorOneOf4 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleLayerSelectorOneOf4.copyWith(...)` or `instanceOfChronicleLayerSelectorOneOf4.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleLayerSelectorOneOf4CWProxy get copyWith =>
      _$ChronicleLayerSelectorOneOf4CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleLayerSelectorOneOf4 _$ChronicleLayerSelectorOneOf4FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleLayerSelectorOneOf4', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type', 'situationIds']);
  final val = ChronicleLayerSelectorOneOf4(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ChronicleLayerSelectorOneOf4TypeEnumEnumMap, v),
    ),
    situationIds: $checkedConvert(
      'situationIds',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChronicleLayerSelectorOneOf4ToJson(
  ChronicleLayerSelectorOneOf4 instance,
) => <String, dynamic>{
  'type': _$ChronicleLayerSelectorOneOf4TypeEnumEnumMap[instance.type]!,
  'situationIds': instance.situationIds,
};

const _$ChronicleLayerSelectorOneOf4TypeEnumEnumMap = {
  ChronicleLayerSelectorOneOf4TypeEnum.situations: 'situations',
};
