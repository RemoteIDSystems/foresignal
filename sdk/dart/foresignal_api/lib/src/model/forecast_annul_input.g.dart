// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_annul_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastAnnulInputCWProxy {
  ForecastAnnulInput reason(String reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastAnnulInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastAnnulInput(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastAnnulInput call({String reason});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastAnnulInput.copyWith(...)` or call `instanceOfForecastAnnulInput.copyWith.fieldName(value)` for a single field.
class _$ForecastAnnulInputCWProxyImpl implements _$ForecastAnnulInputCWProxy {
  const _$ForecastAnnulInputCWProxyImpl(this._value);

  final ForecastAnnulInput _value;

  @override
  ForecastAnnulInput reason(String reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastAnnulInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastAnnulInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastAnnulInput call({Object? reason = const $CopyWithPlaceholder()}) {
    return ForecastAnnulInput(
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String,
    );
  }
}

extension $ForecastAnnulInputCopyWith on ForecastAnnulInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastAnnulInput.copyWith(...)` or `instanceOfForecastAnnulInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastAnnulInputCWProxy get copyWith =>
      _$ForecastAnnulInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastAnnulInput _$ForecastAnnulInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ForecastAnnulInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['reason']);
      final val = ForecastAnnulInput(
        reason: $checkedConvert('reason', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$ForecastAnnulInputToJson(ForecastAnnulInput instance) =>
    <String, dynamic>{'reason': instance.reason};
