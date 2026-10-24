// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analog_result_target.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalogResultTargetCWProxy {
  AnalogResultTarget situationId(String? situationId);

  AnalogResultTarget regionCode(String? regionCode);

  AnalogResultTarget from(DateTime? from);

  AnalogResultTarget to(DateTime? to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalogResultTarget(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalogResultTarget(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalogResultTarget call({
    String? situationId,
    String? regionCode,
    DateTime? from,
    DateTime? to,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalogResultTarget.copyWith(...)` or call `instanceOfAnalogResultTarget.copyWith.fieldName(value)` for a single field.
class _$AnalogResultTargetCWProxyImpl implements _$AnalogResultTargetCWProxy {
  const _$AnalogResultTargetCWProxyImpl(this._value);

  final AnalogResultTarget _value;

  @override
  AnalogResultTarget situationId(String? situationId) =>
      call(situationId: situationId);

  @override
  AnalogResultTarget regionCode(String? regionCode) =>
      call(regionCode: regionCode);

  @override
  AnalogResultTarget from(DateTime? from) => call(from: from);

  @override
  AnalogResultTarget to(DateTime? to) => call(to: to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalogResultTarget(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalogResultTarget(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalogResultTarget call({
    Object? situationId = const $CopyWithPlaceholder(),
    Object? regionCode = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
  }) {
    return AnalogResultTarget(
      situationId: situationId == const $CopyWithPlaceholder()
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String?,
      regionCode: regionCode == const $CopyWithPlaceholder()
          ? _value.regionCode
          // ignore: cast_nullable_to_non_nullable
          : regionCode as String?,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
    );
  }
}

extension $AnalogResultTargetCopyWith on AnalogResultTarget {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalogResultTarget.copyWith(...)` or `instanceOfAnalogResultTarget.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalogResultTargetCWProxy get copyWith =>
      _$AnalogResultTargetCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalogResultTarget _$AnalogResultTargetFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalogResultTarget', json, ($checkedConvert) {
      final val = AnalogResultTarget(
        situationId: $checkedConvert('situationId', (v) => v as String?),
        regionCode: $checkedConvert('regionCode', (v) => v as String?),
        from: $checkedConvert(
          'from',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        to: $checkedConvert(
          'to',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AnalogResultTargetToJson(AnalogResultTarget instance) =>
    <String, dynamic>{
      'situationId': ?instance.situationId,
      'regionCode': ?instance.regionCode,
      'from': ?instance.from?.toIso8601String(),
      'to': ?instance.to?.toIso8601String(),
    };
