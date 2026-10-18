// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exposure_population.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExposurePopulationCWProxy {
  ExposurePopulation status(ExposurePopulationStatusEnum status);

  ExposurePopulation attribution(List<Attribution>? attribution);

  ExposurePopulation estimate(num? estimate);

  ExposurePopulation epoch(int? epoch);

  ExposurePopulation method(String? method);

  ExposurePopulation note(String? note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposurePopulation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposurePopulation(...).copyWith(id: 12, name: "My name")
  /// ```
  ExposurePopulation call({
    ExposurePopulationStatusEnum status,
    List<Attribution>? attribution,
    num? estimate,
    int? epoch,
    String? method,
    String? note,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExposurePopulation.copyWith(...)` or call `instanceOfExposurePopulation.copyWith.fieldName(value)` for a single field.
class _$ExposurePopulationCWProxyImpl implements _$ExposurePopulationCWProxy {
  const _$ExposurePopulationCWProxyImpl(this._value);

  final ExposurePopulation _value;

  @override
  ExposurePopulation status(ExposurePopulationStatusEnum status) =>
      call(status: status);

  @override
  ExposurePopulation attribution(List<Attribution>? attribution) =>
      call(attribution: attribution);

  @override
  ExposurePopulation estimate(num? estimate) => call(estimate: estimate);

  @override
  ExposurePopulation epoch(int? epoch) => call(epoch: epoch);

  @override
  ExposurePopulation method(String? method) => call(method: method);

  @override
  ExposurePopulation note(String? note) => call(note: note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposurePopulation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposurePopulation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExposurePopulation call({
    Object? status = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? estimate = const $CopyWithPlaceholder(),
    Object? epoch = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
  }) {
    return ExposurePopulation(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ExposurePopulationStatusEnum,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as List<Attribution>?,
      estimate: estimate == const $CopyWithPlaceholder()
          ? _value.estimate
          // ignore: cast_nullable_to_non_nullable
          : estimate as num?,
      epoch: epoch == const $CopyWithPlaceholder()
          ? _value.epoch
          // ignore: cast_nullable_to_non_nullable
          : epoch as int?,
      method: method == const $CopyWithPlaceholder()
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as String?,
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
    );
  }
}

extension $ExposurePopulationCopyWith on ExposurePopulation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExposurePopulation.copyWith(...)` or `instanceOfExposurePopulation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExposurePopulationCWProxy get copyWith =>
      _$ExposurePopulationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExposurePopulation _$ExposurePopulationFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ExposurePopulation', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status']);
      final val = ExposurePopulation(
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$ExposurePopulationStatusEnumEnumMap, v),
        ),
        attribution: $checkedConvert(
          'attribution',
          (v) => (v as List<dynamic>?)
              ?.map((e) => Attribution.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        estimate: $checkedConvert('estimate', (v) => v as num?),
        epoch: $checkedConvert('epoch', (v) => (v as num?)?.toInt()),
        method: $checkedConvert('method', (v) => v as String?),
        note: $checkedConvert('note', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ExposurePopulationToJson(ExposurePopulation instance) =>
    <String, dynamic>{
      'status': _$ExposurePopulationStatusEnumEnumMap[instance.status]!,
      'attribution': ?instance.attribution?.map((e) => e.toJson()).toList(),
      'estimate': ?instance.estimate,
      'epoch': ?instance.epoch,
      'method': ?instance.method,
      'note': ?instance.note,
    };

const _$ExposurePopulationStatusEnumEnumMap = {
  ExposurePopulationStatusEnum.ok: 'ok',
  ExposurePopulationStatusEnum.unavailable: 'unavailable',
  ExposurePopulationStatusEnum.notInstalled: 'not-installed',
};
