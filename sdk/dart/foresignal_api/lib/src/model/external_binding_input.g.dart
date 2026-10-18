// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'external_binding_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExternalBindingInputCWProxy {
  ExternalBindingInput streamId(String streamId);

  ExternalBindingInput target(ExternalBindingInputTargetEnum target);

  ExternalBindingInput config(Map<String, Object>? config);

  ExternalBindingInput filter(List<Map<String, Object>>? filter);

  ExternalBindingInput enabled(bool? enabled);

  ExternalBindingInput backfill(bool? backfill);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalBindingInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalBindingInput(...).copyWith(id: 12, name: "My name")
  /// ```
  ExternalBindingInput call({
    String streamId,
    ExternalBindingInputTargetEnum target,
    Map<String, Object>? config,
    List<Map<String, Object>>? filter,
    bool? enabled,
    bool? backfill,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExternalBindingInput.copyWith(...)` or call `instanceOfExternalBindingInput.copyWith.fieldName(value)` for a single field.
class _$ExternalBindingInputCWProxyImpl
    implements _$ExternalBindingInputCWProxy {
  const _$ExternalBindingInputCWProxyImpl(this._value);

  final ExternalBindingInput _value;

  @override
  ExternalBindingInput streamId(String streamId) => call(streamId: streamId);

  @override
  ExternalBindingInput target(ExternalBindingInputTargetEnum target) =>
      call(target: target);

  @override
  ExternalBindingInput config(Map<String, Object>? config) =>
      call(config: config);

  @override
  ExternalBindingInput filter(List<Map<String, Object>>? filter) =>
      call(filter: filter);

  @override
  ExternalBindingInput enabled(bool? enabled) => call(enabled: enabled);

  @override
  ExternalBindingInput backfill(bool? backfill) => call(backfill: backfill);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalBindingInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalBindingInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExternalBindingInput call({
    Object? streamId = const $CopyWithPlaceholder(),
    Object? target = const $CopyWithPlaceholder(),
    Object? config = const $CopyWithPlaceholder(),
    Object? filter = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? backfill = const $CopyWithPlaceholder(),
  }) {
    return ExternalBindingInput(
      streamId: streamId == const $CopyWithPlaceholder() || streamId == null
          ? _value.streamId
          // ignore: cast_nullable_to_non_nullable
          : streamId as String,
      target: target == const $CopyWithPlaceholder() || target == null
          ? _value.target
          // ignore: cast_nullable_to_non_nullable
          : target as ExternalBindingInputTargetEnum,
      config: config == const $CopyWithPlaceholder()
          ? _value.config
          // ignore: cast_nullable_to_non_nullable
          : config as Map<String, Object>?,
      filter: filter == const $CopyWithPlaceholder()
          ? _value.filter
          // ignore: cast_nullable_to_non_nullable
          : filter as List<Map<String, Object>>?,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      backfill: backfill == const $CopyWithPlaceholder()
          ? _value.backfill
          // ignore: cast_nullable_to_non_nullable
          : backfill as bool?,
    );
  }
}

extension $ExternalBindingInputCopyWith on ExternalBindingInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExternalBindingInput.copyWith(...)` or `instanceOfExternalBindingInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExternalBindingInputCWProxy get copyWith =>
      _$ExternalBindingInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExternalBindingInput _$ExternalBindingInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExternalBindingInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['streamId', 'target']);
  final val = ExternalBindingInput(
    streamId: $checkedConvert('streamId', (v) => v as String),
    target: $checkedConvert(
      'target',
      (v) => $enumDecode(_$ExternalBindingInputTargetEnumEnumMap, v),
    ),
    config: $checkedConvert(
      'config',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    filter: $checkedConvert(
      'filter',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => (e as Map<String, dynamic>).map(
              (k, e) => MapEntry(k, e as Object),
            ),
          )
          .toList(),
    ),
    enabled: $checkedConvert('enabled', (v) => v as bool? ?? true),
    backfill: $checkedConvert('backfill', (v) => v as bool? ?? true),
  );
  return val;
});

Map<String, dynamic> _$ExternalBindingInputToJson(
  ExternalBindingInput instance,
) => <String, dynamic>{
  'streamId': instance.streamId,
  'target': _$ExternalBindingInputTargetEnumEnumMap[instance.target]!,
  'config': ?instance.config,
  'filter': ?instance.filter,
  'enabled': ?instance.enabled,
  'backfill': ?instance.backfill,
};

const _$ExternalBindingInputTargetEnumEnumMap = {
  ExternalBindingInputTargetEnum.series: 'series',
  ExternalBindingInputTargetEnum.canonicalFamily: 'canonical-family',
  ExternalBindingInputTargetEnum.social: 'social',
  ExternalBindingInputTargetEnum.news: 'news',
  ExternalBindingInputTargetEnum.evidence: 'evidence',
  ExternalBindingInputTargetEnum.indicator: 'indicator',
  ExternalBindingInputTargetEnum.forecast: 'forecast',
  ExternalBindingInputTargetEnum.place: 'place',
  ExternalBindingInputTargetEnum.situation: 'situation',
  ExternalBindingInputTargetEnum.chronicle: 'chronicle',
};
