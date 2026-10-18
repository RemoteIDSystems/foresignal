// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'doctor_options.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DoctorOptionsCWProxy {
  DoctorOptions probe(bool? probe);

  DoctorOptions llm(DoctorOptionsLlmEnum? llm);

  DoctorOptions detail(DoctorOptionsDetailEnum? detail);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorOptions(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorOptions(...).copyWith(id: 12, name: "My name")
  /// ```
  DoctorOptions call({
    bool? probe,
    DoctorOptionsLlmEnum? llm,
    DoctorOptionsDetailEnum? detail,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDoctorOptions.copyWith(...)` or call `instanceOfDoctorOptions.copyWith.fieldName(value)` for a single field.
class _$DoctorOptionsCWProxyImpl implements _$DoctorOptionsCWProxy {
  const _$DoctorOptionsCWProxyImpl(this._value);

  final DoctorOptions _value;

  @override
  DoctorOptions probe(bool? probe) => call(probe: probe);

  @override
  DoctorOptions llm(DoctorOptionsLlmEnum? llm) => call(llm: llm);

  @override
  DoctorOptions detail(DoctorOptionsDetailEnum? detail) => call(detail: detail);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorOptions(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorOptions(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DoctorOptions call({
    Object? probe = const $CopyWithPlaceholder(),
    Object? llm = const $CopyWithPlaceholder(),
    Object? detail = const $CopyWithPlaceholder(),
  }) {
    return DoctorOptions(
      probe: probe == const $CopyWithPlaceholder()
          ? _value.probe
          // ignore: cast_nullable_to_non_nullable
          : probe as bool?,
      llm: llm == const $CopyWithPlaceholder()
          ? _value.llm
          // ignore: cast_nullable_to_non_nullable
          : llm as DoctorOptionsLlmEnum?,
      detail: detail == const $CopyWithPlaceholder()
          ? _value.detail
          // ignore: cast_nullable_to_non_nullable
          : detail as DoctorOptionsDetailEnum?,
    );
  }
}

extension $DoctorOptionsCopyWith on DoctorOptions {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDoctorOptions.copyWith(...)` or `instanceOfDoctorOptions.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DoctorOptionsCWProxy get copyWith => _$DoctorOptionsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DoctorOptions _$DoctorOptionsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DoctorOptions', json, ($checkedConvert) {
      final val = DoctorOptions(
        probe: $checkedConvert('probe', (v) => v as bool? ?? true),
        llm: $checkedConvert(
          'llm',
          (v) => $enumDecodeNullable(_$DoctorOptionsLlmEnumEnumMap, v),
        ),
        detail: $checkedConvert(
          'detail',
          (v) => $enumDecodeNullable(_$DoctorOptionsDetailEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$DoctorOptionsToJson(DoctorOptions instance) =>
    <String, dynamic>{
      'probe': ?instance.probe,
      'llm': ?_$DoctorOptionsLlmEnumEnumMap[instance.llm],
      'detail': ?_$DoctorOptionsDetailEnumEnumMap[instance.detail],
    };

const _$DoctorOptionsLlmEnumEnumMap = {
  DoctorOptionsLlmEnum.auto: 'auto',
  DoctorOptionsLlmEnum.on_: 'on',
  DoctorOptionsLlmEnum.off: 'off',
};

const _$DoctorOptionsDetailEnumEnumMap = {
  DoctorOptionsDetailEnum.compact: 'compact',
  DoctorOptionsDetailEnum.full: 'full',
};
