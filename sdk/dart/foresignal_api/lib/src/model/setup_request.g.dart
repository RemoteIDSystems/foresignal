// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'setup_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SetupRequestCWProxy {
  SetupRequest admin(SetupRequestAdmin admin);

  SetupRequest smtp(SmtpSettingsInput? smtp);

  SetupRequest sources(List<String>? sources);

  SetupRequest thresholds(ResourceThresholds? thresholds);

  SetupRequest retention(Map<String, Object>? retention);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SetupRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SetupRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  SetupRequest call({
    SetupRequestAdmin admin,
    SmtpSettingsInput? smtp,
    List<String>? sources,
    ResourceThresholds? thresholds,
    Map<String, Object>? retention,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSetupRequest.copyWith(...)` or call `instanceOfSetupRequest.copyWith.fieldName(value)` for a single field.
class _$SetupRequestCWProxyImpl implements _$SetupRequestCWProxy {
  const _$SetupRequestCWProxyImpl(this._value);

  final SetupRequest _value;

  @override
  SetupRequest admin(SetupRequestAdmin admin) => call(admin: admin);

  @override
  SetupRequest smtp(SmtpSettingsInput? smtp) => call(smtp: smtp);

  @override
  SetupRequest sources(List<String>? sources) => call(sources: sources);

  @override
  SetupRequest thresholds(ResourceThresholds? thresholds) =>
      call(thresholds: thresholds);

  @override
  SetupRequest retention(Map<String, Object>? retention) =>
      call(retention: retention);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SetupRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SetupRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SetupRequest call({
    Object? admin = const $CopyWithPlaceholder(),
    Object? smtp = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? thresholds = const $CopyWithPlaceholder(),
    Object? retention = const $CopyWithPlaceholder(),
  }) {
    return SetupRequest(
      admin: admin == const $CopyWithPlaceholder() || admin == null
          ? _value.admin
          // ignore: cast_nullable_to_non_nullable
          : admin as SetupRequestAdmin,
      smtp: smtp == const $CopyWithPlaceholder()
          ? _value.smtp
          // ignore: cast_nullable_to_non_nullable
          : smtp as SmtpSettingsInput?,
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>?,
      thresholds: thresholds == const $CopyWithPlaceholder()
          ? _value.thresholds
          // ignore: cast_nullable_to_non_nullable
          : thresholds as ResourceThresholds?,
      retention: retention == const $CopyWithPlaceholder()
          ? _value.retention
          // ignore: cast_nullable_to_non_nullable
          : retention as Map<String, Object>?,
    );
  }
}

extension $SetupRequestCopyWith on SetupRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSetupRequest.copyWith(...)` or `instanceOfSetupRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SetupRequestCWProxy get copyWith => _$SetupRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SetupRequest _$SetupRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SetupRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['admin']);
      final val = SetupRequest(
        admin: $checkedConvert(
          'admin',
          (v) => SetupRequestAdmin.fromJson(v as Map<String, dynamic>),
        ),
        smtp: $checkedConvert(
          'smtp',
          (v) => v == null
              ? null
              : SmtpSettingsInput.fromJson(v as Map<String, dynamic>),
        ),
        sources: $checkedConvert(
          'sources',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        thresholds: $checkedConvert(
          'thresholds',
          (v) => v == null
              ? null
              : ResourceThresholds.fromJson(v as Map<String, dynamic>),
        ),
        retention: $checkedConvert(
          'retention',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SetupRequestToJson(SetupRequest instance) =>
    <String, dynamic>{
      'admin': instance.admin.toJson(),
      'smtp': ?instance.smtp?.toJson(),
      'sources': ?instance.sources,
      'thresholds': ?instance.thresholds?.toJson(),
      'retention': ?instance.retention,
    };
