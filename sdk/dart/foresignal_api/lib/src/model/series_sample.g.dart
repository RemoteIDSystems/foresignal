// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'series_sample.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SeriesSampleCWProxy {
  SeriesSample time(DateTime time);

  SeriesSample kind(SeriesSampleKindEnum kind);

  SeriesSample value(num? value);

  SeriesSample u(num? u);

  SeriesSample v(num? v);

  SeriesSample derived(SampleValueDerived? derived);

  SeriesSample frameKey(String? frameKey);

  SeriesSample run(String? run);

  SeriesSample step(int? step);

  SeriesSample status(SampleStatus status);

  SeriesSample statusText(String? statusText);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesSample(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesSample(...).copyWith(id: 12, name: "My name")
  /// ```
  SeriesSample call({
    DateTime time,
    SeriesSampleKindEnum kind,
    num? value,
    num? u,
    num? v,
    SampleValueDerived? derived,
    String? frameKey,
    String? run,
    int? step,
    SampleStatus status,
    String? statusText,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSeriesSample.copyWith(...)` or call `instanceOfSeriesSample.copyWith.fieldName(value)` for a single field.
class _$SeriesSampleCWProxyImpl implements _$SeriesSampleCWProxy {
  const _$SeriesSampleCWProxyImpl(this._value);

  final SeriesSample _value;

  @override
  SeriesSample time(DateTime time) => call(time: time);

  @override
  SeriesSample kind(SeriesSampleKindEnum kind) => call(kind: kind);

  @override
  SeriesSample value(num? value) => call(value: value);

  @override
  SeriesSample u(num? u) => call(u: u);

  @override
  SeriesSample v(num? v) => call(v: v);

  @override
  SeriesSample derived(SampleValueDerived? derived) => call(derived: derived);

  @override
  SeriesSample frameKey(String? frameKey) => call(frameKey: frameKey);

  @override
  SeriesSample run(String? run) => call(run: run);

  @override
  SeriesSample step(int? step) => call(step: step);

  @override
  SeriesSample status(SampleStatus status) => call(status: status);

  @override
  SeriesSample statusText(String? statusText) => call(statusText: statusText);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesSample(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesSample(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SeriesSample call({
    Object? time = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
    Object? u = const $CopyWithPlaceholder(),
    Object? v = const $CopyWithPlaceholder(),
    Object? derived = const $CopyWithPlaceholder(),
    Object? frameKey = const $CopyWithPlaceholder(),
    Object? run = const $CopyWithPlaceholder(),
    Object? step = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
  }) {
    return SeriesSample(
      time: time == const $CopyWithPlaceholder() || time == null
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as DateTime,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as SeriesSampleKindEnum,
      value: value == const $CopyWithPlaceholder()
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num?,
      u: u == const $CopyWithPlaceholder()
          ? _value.u
          // ignore: cast_nullable_to_non_nullable
          : u as num?,
      v: v == const $CopyWithPlaceholder()
          ? _value.v
          // ignore: cast_nullable_to_non_nullable
          : v as num?,
      derived: derived == const $CopyWithPlaceholder()
          ? _value.derived
          // ignore: cast_nullable_to_non_nullable
          : derived as SampleValueDerived?,
      frameKey: frameKey == const $CopyWithPlaceholder()
          ? _value.frameKey
          // ignore: cast_nullable_to_non_nullable
          : frameKey as String?,
      run: run == const $CopyWithPlaceholder()
          ? _value.run
          // ignore: cast_nullable_to_non_nullable
          : run as String?,
      step: step == const $CopyWithPlaceholder()
          ? _value.step
          // ignore: cast_nullable_to_non_nullable
          : step as int?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SampleStatus,
      statusText: statusText == const $CopyWithPlaceholder()
          ? _value.statusText
          // ignore: cast_nullable_to_non_nullable
          : statusText as String?,
    );
  }
}

extension $SeriesSampleCopyWith on SeriesSample {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSeriesSample.copyWith(...)` or `instanceOfSeriesSample.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SeriesSampleCWProxy get copyWith => _$SeriesSampleCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SeriesSample _$SeriesSampleFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SeriesSample', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['time', 'kind', 'status']);
      final val = SeriesSample(
        time: $checkedConvert('time', (v) => DateTime.parse(v as String)),
        kind: $checkedConvert(
          'kind',
          (v) => $enumDecode(_$SeriesSampleKindEnumEnumMap, v),
        ),
        value: $checkedConvert('value', (v) => v as num?),
        u: $checkedConvert('u', (v) => v as num?),
        v: $checkedConvert('v', (v) => v as num?),
        derived: $checkedConvert(
          'derived',
          (v) => v == null
              ? null
              : SampleValueDerived.fromJson(v as Map<String, dynamic>),
        ),
        frameKey: $checkedConvert('frameKey', (v) => v as String?),
        run: $checkedConvert('run', (v) => v as String?),
        step: $checkedConvert('step', (v) => (v as num?)?.toInt()),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$SampleStatusEnumMap, v),
        ),
        statusText: $checkedConvert('statusText', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SeriesSampleToJson(SeriesSample instance) =>
    <String, dynamic>{
      'time': instance.time.toIso8601String(),
      'kind': _$SeriesSampleKindEnumEnumMap[instance.kind]!,
      'value': ?instance.value,
      'u': ?instance.u,
      'v': ?instance.v,
      'derived': ?instance.derived?.toJson(),
      'frameKey': ?instance.frameKey,
      'run': ?instance.run,
      'step': ?instance.step,
      'status': _$SampleStatusEnumMap[instance.status]!,
      'statusText': ?instance.statusText,
    };

const _$SeriesSampleKindEnumEnumMap = {
  SeriesSampleKindEnum.observed: 'observed',
  SeriesSampleKindEnum.forecast: 'forecast',
};

const _$SampleStatusEnumMap = {
  SampleStatus.ok: 'ok',
  SampleStatus.noCoverage: 'no-coverage',
  SampleStatus.noData: 'no-data',
  SampleStatus.stale: 'stale',
  SampleStatus.unavailable: 'unavailable',
  SampleStatus.stalePosition: 'stale-position',
};
