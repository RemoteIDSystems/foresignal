// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'native_push_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NativePushPayloadCWProxy {
  NativePushPayload apns(NativePushPayloadApns? apns);

  NativePushPayload fcm(NativePushPayloadFcm? fcm);

  NativePushPayload exercise(bool? exercise);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NativePushPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NativePushPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  NativePushPayload call({
    NativePushPayloadApns? apns,
    NativePushPayloadFcm? fcm,
    bool? exercise,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNativePushPayload.copyWith(...)` or call `instanceOfNativePushPayload.copyWith.fieldName(value)` for a single field.
class _$NativePushPayloadCWProxyImpl implements _$NativePushPayloadCWProxy {
  const _$NativePushPayloadCWProxyImpl(this._value);

  final NativePushPayload _value;

  @override
  NativePushPayload apns(NativePushPayloadApns? apns) => call(apns: apns);

  @override
  NativePushPayload fcm(NativePushPayloadFcm? fcm) => call(fcm: fcm);

  @override
  NativePushPayload exercise(bool? exercise) => call(exercise: exercise);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NativePushPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NativePushPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NativePushPayload call({
    Object? apns = const $CopyWithPlaceholder(),
    Object? fcm = const $CopyWithPlaceholder(),
    Object? exercise = const $CopyWithPlaceholder(),
  }) {
    return NativePushPayload(
      apns: apns == const $CopyWithPlaceholder()
          ? _value.apns
          // ignore: cast_nullable_to_non_nullable
          : apns as NativePushPayloadApns?,
      fcm: fcm == const $CopyWithPlaceholder()
          ? _value.fcm
          // ignore: cast_nullable_to_non_nullable
          : fcm as NativePushPayloadFcm?,
      exercise: exercise == const $CopyWithPlaceholder()
          ? _value.exercise
          // ignore: cast_nullable_to_non_nullable
          : exercise as bool?,
    );
  }
}

extension $NativePushPayloadCopyWith on NativePushPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNativePushPayload.copyWith(...)` or `instanceOfNativePushPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NativePushPayloadCWProxy get copyWith =>
      _$NativePushPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NativePushPayload _$NativePushPayloadFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NativePushPayload', json, ($checkedConvert) {
      final val = NativePushPayload(
        apns: $checkedConvert(
          'apns',
          (v) => v == null
              ? null
              : NativePushPayloadApns.fromJson(v as Map<String, dynamic>),
        ),
        fcm: $checkedConvert(
          'fcm',
          (v) => v == null
              ? null
              : NativePushPayloadFcm.fromJson(v as Map<String, dynamic>),
        ),
        exercise: $checkedConvert('exercise', (v) => v as bool? ?? false),
      );
      return val;
    });

Map<String, dynamic> _$NativePushPayloadToJson(NativePushPayload instance) =>
    <String, dynamic>{
      'apns': ?instance.apns?.toJson(),
      'fcm': ?instance.fcm?.toJson(),
      'exercise': ?instance.exercise,
    };
