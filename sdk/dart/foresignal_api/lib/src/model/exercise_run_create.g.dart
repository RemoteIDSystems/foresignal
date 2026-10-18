// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exercise_run_create.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExerciseRunCreateCWProxy {
  ExerciseRunCreate name(String name);

  ExerciseRunCreate ruleIds(List<String>? ruleIds);

  ExerciseRunCreate tag(String? tag);

  ExerciseRunCreate from(DateTime? from);

  ExerciseRunCreate to(DateTime? to);

  ExerciseRunCreate fromSeq(int? fromSeq);

  ExerciseRunCreate toSeq(int? toSeq);

  ExerciseRunCreate speed(ExerciseRunCreateSpeedEnum? speed);

  ExerciseRunCreate channelIds(List<String>? channelIds);

  ExerciseRunCreate enrich(bool? enrich);

  ExerciseRunCreate dryRun(bool? dryRun);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExerciseRunCreate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExerciseRunCreate(...).copyWith(id: 12, name: "My name")
  /// ```
  ExerciseRunCreate call({
    String name,
    List<String>? ruleIds,
    String? tag,
    DateTime? from,
    DateTime? to,
    int? fromSeq,
    int? toSeq,
    ExerciseRunCreateSpeedEnum? speed,
    List<String>? channelIds,
    bool? enrich,
    bool? dryRun,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExerciseRunCreate.copyWith(...)` or call `instanceOfExerciseRunCreate.copyWith.fieldName(value)` for a single field.
class _$ExerciseRunCreateCWProxyImpl implements _$ExerciseRunCreateCWProxy {
  const _$ExerciseRunCreateCWProxyImpl(this._value);

  final ExerciseRunCreate _value;

  @override
  ExerciseRunCreate name(String name) => call(name: name);

  @override
  ExerciseRunCreate ruleIds(List<String>? ruleIds) => call(ruleIds: ruleIds);

  @override
  ExerciseRunCreate tag(String? tag) => call(tag: tag);

  @override
  ExerciseRunCreate from(DateTime? from) => call(from: from);

  @override
  ExerciseRunCreate to(DateTime? to) => call(to: to);

  @override
  ExerciseRunCreate fromSeq(int? fromSeq) => call(fromSeq: fromSeq);

  @override
  ExerciseRunCreate toSeq(int? toSeq) => call(toSeq: toSeq);

  @override
  ExerciseRunCreate speed(ExerciseRunCreateSpeedEnum? speed) =>
      call(speed: speed);

  @override
  ExerciseRunCreate channelIds(List<String>? channelIds) =>
      call(channelIds: channelIds);

  @override
  ExerciseRunCreate enrich(bool? enrich) => call(enrich: enrich);

  @override
  ExerciseRunCreate dryRun(bool? dryRun) => call(dryRun: dryRun);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExerciseRunCreate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExerciseRunCreate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExerciseRunCreate call({
    Object? name = const $CopyWithPlaceholder(),
    Object? ruleIds = const $CopyWithPlaceholder(),
    Object? tag = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? fromSeq = const $CopyWithPlaceholder(),
    Object? toSeq = const $CopyWithPlaceholder(),
    Object? speed = const $CopyWithPlaceholder(),
    Object? channelIds = const $CopyWithPlaceholder(),
    Object? enrich = const $CopyWithPlaceholder(),
    Object? dryRun = const $CopyWithPlaceholder(),
  }) {
    return ExerciseRunCreate(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      ruleIds: ruleIds == const $CopyWithPlaceholder()
          ? _value.ruleIds
          // ignore: cast_nullable_to_non_nullable
          : ruleIds as List<String>?,
      tag: tag == const $CopyWithPlaceholder()
          ? _value.tag
          // ignore: cast_nullable_to_non_nullable
          : tag as String?,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
      fromSeq: fromSeq == const $CopyWithPlaceholder()
          ? _value.fromSeq
          // ignore: cast_nullable_to_non_nullable
          : fromSeq as int?,
      toSeq: toSeq == const $CopyWithPlaceholder()
          ? _value.toSeq
          // ignore: cast_nullable_to_non_nullable
          : toSeq as int?,
      speed: speed == const $CopyWithPlaceholder()
          ? _value.speed
          // ignore: cast_nullable_to_non_nullable
          : speed as ExerciseRunCreateSpeedEnum?,
      channelIds: channelIds == const $CopyWithPlaceholder()
          ? _value.channelIds
          // ignore: cast_nullable_to_non_nullable
          : channelIds as List<String>?,
      enrich: enrich == const $CopyWithPlaceholder()
          ? _value.enrich
          // ignore: cast_nullable_to_non_nullable
          : enrich as bool?,
      dryRun: dryRun == const $CopyWithPlaceholder()
          ? _value.dryRun
          // ignore: cast_nullable_to_non_nullable
          : dryRun as bool?,
    );
  }
}

extension $ExerciseRunCreateCopyWith on ExerciseRunCreate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExerciseRunCreate.copyWith(...)` or `instanceOfExerciseRunCreate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExerciseRunCreateCWProxy get copyWith =>
      _$ExerciseRunCreateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExerciseRunCreate _$ExerciseRunCreateFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ExerciseRunCreate', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name']);
      final val = ExerciseRunCreate(
        name: $checkedConvert('name', (v) => v as String),
        ruleIds: $checkedConvert(
          'ruleIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        tag: $checkedConvert('tag', (v) => v as String?),
        from: $checkedConvert(
          'from',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        to: $checkedConvert(
          'to',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        fromSeq: $checkedConvert('fromSeq', (v) => (v as num?)?.toInt()),
        toSeq: $checkedConvert('toSeq', (v) => (v as num?)?.toInt()),
        speed: $checkedConvert(
          'speed',
          (v) => $enumDecodeNullable(_$ExerciseRunCreateSpeedEnumEnumMap, v),
        ),
        channelIds: $checkedConvert(
          'channelIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        enrich: $checkedConvert('enrich', (v) => v as bool? ?? false),
        dryRun: $checkedConvert('dryRun', (v) => v as bool? ?? false),
      );
      return val;
    });

Map<String, dynamic> _$ExerciseRunCreateToJson(ExerciseRunCreate instance) =>
    <String, dynamic>{
      'name': instance.name,
      'ruleIds': ?instance.ruleIds,
      'tag': ?instance.tag,
      'from': ?instance.from?.toIso8601String(),
      'to': ?instance.to?.toIso8601String(),
      'fromSeq': ?instance.fromSeq,
      'toSeq': ?instance.toSeq,
      'speed': ?_$ExerciseRunCreateSpeedEnumEnumMap[instance.speed],
      'channelIds': ?instance.channelIds,
      'enrich': ?instance.enrich,
      'dryRun': ?instance.dryRun,
    };

const _$ExerciseRunCreateSpeedEnumEnumMap = {
  ExerciseRunCreateSpeedEnum.realtime: 'realtime',
  ExerciseRunCreateSpeedEnum.x10: 'x10',
  ExerciseRunCreateSpeedEnum.x60: 'x60',
  ExerciseRunCreateSpeedEnum.max: 'max',
};
