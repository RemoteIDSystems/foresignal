// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_layer_selector_one_of.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleLayerSelectorOneOfCWProxy {
  ChronicleLayerSelectorOneOf type(ChronicleLayerSelectorOneOfTypeEnum type);

  ChronicleLayerSelectorOneOf sources(List<String>? sources);

  ChronicleLayerSelectorOneOf kinds(List<String>? kinds);

  ChronicleLayerSelectorOneOf conditions(List<Condition>? conditions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerSelectorOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerSelectorOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleLayerSelectorOneOf call({
    ChronicleLayerSelectorOneOfTypeEnum type,
    List<String>? sources,
    List<String>? kinds,
    List<Condition>? conditions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleLayerSelectorOneOf.copyWith(...)` or call `instanceOfChronicleLayerSelectorOneOf.copyWith.fieldName(value)` for a single field.
class _$ChronicleLayerSelectorOneOfCWProxyImpl
    implements _$ChronicleLayerSelectorOneOfCWProxy {
  const _$ChronicleLayerSelectorOneOfCWProxyImpl(this._value);

  final ChronicleLayerSelectorOneOf _value;

  @override
  ChronicleLayerSelectorOneOf type(ChronicleLayerSelectorOneOfTypeEnum type) =>
      call(type: type);

  @override
  ChronicleLayerSelectorOneOf sources(List<String>? sources) =>
      call(sources: sources);

  @override
  ChronicleLayerSelectorOneOf kinds(List<String>? kinds) => call(kinds: kinds);

  @override
  ChronicleLayerSelectorOneOf conditions(List<Condition>? conditions) =>
      call(conditions: conditions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLayerSelectorOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLayerSelectorOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleLayerSelectorOneOf call({
    Object? type = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? conditions = const $CopyWithPlaceholder(),
  }) {
    return ChronicleLayerSelectorOneOf(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ChronicleLayerSelectorOneOfTypeEnum,
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>?,
      kinds: kinds == const $CopyWithPlaceholder()
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>?,
      conditions: conditions == const $CopyWithPlaceholder()
          ? _value.conditions
          // ignore: cast_nullable_to_non_nullable
          : conditions as List<Condition>?,
    );
  }
}

extension $ChronicleLayerSelectorOneOfCopyWith on ChronicleLayerSelectorOneOf {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleLayerSelectorOneOf.copyWith(...)` or `instanceOfChronicleLayerSelectorOneOf.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleLayerSelectorOneOfCWProxy get copyWith =>
      _$ChronicleLayerSelectorOneOfCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleLayerSelectorOneOf _$ChronicleLayerSelectorOneOfFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleLayerSelectorOneOf', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type']);
  final val = ChronicleLayerSelectorOneOf(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ChronicleLayerSelectorOneOfTypeEnumEnumMap, v),
    ),
    sources: $checkedConvert(
      'sources',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    kinds: $checkedConvert(
      'kinds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    conditions: $checkedConvert(
      'conditions',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Condition.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChronicleLayerSelectorOneOfToJson(
  ChronicleLayerSelectorOneOf instance,
) => <String, dynamic>{
  'type': _$ChronicleLayerSelectorOneOfTypeEnumEnumMap[instance.type]!,
  'sources': ?instance.sources,
  'kinds': ?instance.kinds,
  'conditions': ?instance.conditions?.map((e) => e.toJson()).toList(),
};

const _$ChronicleLayerSelectorOneOfTypeEnumEnumMap = {
  ChronicleLayerSelectorOneOfTypeEnum.records: 'records',
};
