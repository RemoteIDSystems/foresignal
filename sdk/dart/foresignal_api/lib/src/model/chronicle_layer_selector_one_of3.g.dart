// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_layer_selector_one_of3.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleLayerSelectorOneOf3CWProxy {
  ChronicleLayerSelectorOneOf3 type(ChronicleLayerSelectorOneOf3TypeEnum type);

  ChronicleLayerSelectorOneOf3 entityIds(List<String> entityIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerSelectorOneOf3(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerSelectorOneOf3(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleLayerSelectorOneOf3 call({
    ChronicleLayerSelectorOneOf3TypeEnum type,
    List<String> entityIds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleLayerSelectorOneOf3.copyWith(...)` or call `instanceOfChronicleLayerSelectorOneOf3.copyWith.fieldName(value)` for a single field.
class _$ChronicleLayerSelectorOneOf3CWProxyImpl
    implements _$ChronicleLayerSelectorOneOf3CWProxy {
  const _$ChronicleLayerSelectorOneOf3CWProxyImpl(this._value);

  final ChronicleLayerSelectorOneOf3 _value;

  @override
  ChronicleLayerSelectorOneOf3 type(
    ChronicleLayerSelectorOneOf3TypeEnum type,
  ) => call(type: type);

  @override
  ChronicleLayerSelectorOneOf3 entityIds(List<String> entityIds) =>
      call(entityIds: entityIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerSelectorOneOf3(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerSelectorOneOf3(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleLayerSelectorOneOf3 call({
    Object? type = const $CopyWithPlaceholder(),
    Object? entityIds = const $CopyWithPlaceholder(),
  }) {
    return ChronicleLayerSelectorOneOf3(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ChronicleLayerSelectorOneOf3TypeEnum,
      entityIds: entityIds == const $CopyWithPlaceholder() || entityIds == null
          ? _value.entityIds
          // ignore: cast_nullable_to_non_nullable
          : entityIds as List<String>,
    );
  }
}

extension $ChronicleLayerSelectorOneOf3CopyWith
    on ChronicleLayerSelectorOneOf3 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleLayerSelectorOneOf3.copyWith(...)` or `instanceOfChronicleLayerSelectorOneOf3.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleLayerSelectorOneOf3CWProxy get copyWith =>
      _$ChronicleLayerSelectorOneOf3CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleLayerSelectorOneOf3 _$ChronicleLayerSelectorOneOf3FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleLayerSelectorOneOf3', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type', 'entityIds']);
  final val = ChronicleLayerSelectorOneOf3(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ChronicleLayerSelectorOneOf3TypeEnumEnumMap, v),
    ),
    entityIds: $checkedConvert(
      'entityIds',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChronicleLayerSelectorOneOf3ToJson(
  ChronicleLayerSelectorOneOf3 instance,
) => <String, dynamic>{
  'type': _$ChronicleLayerSelectorOneOf3TypeEnumEnumMap[instance.type]!,
  'entityIds': instance.entityIds,
};

const _$ChronicleLayerSelectorOneOf3TypeEnumEnumMap = {
  ChronicleLayerSelectorOneOf3TypeEnum.entities: 'entities',
};
