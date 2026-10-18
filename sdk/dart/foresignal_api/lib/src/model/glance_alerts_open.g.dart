// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'glance_alerts_open.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GlanceAlertsOpenCWProxy {
  GlanceAlertsOpen critical(int? critical);

  GlanceAlertsOpen warning(int? warning);

  GlanceAlertsOpen info(int? info);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GlanceAlertsOpen(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GlanceAlertsOpen(...).copyWith(id: 12, name: "My name")
  /// ```
  GlanceAlertsOpen call({int? critical, int? warning, int? info});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGlanceAlertsOpen.copyWith(...)` or call `instanceOfGlanceAlertsOpen.copyWith.fieldName(value)` for a single field.
class _$GlanceAlertsOpenCWProxyImpl implements _$GlanceAlertsOpenCWProxy {
  const _$GlanceAlertsOpenCWProxyImpl(this._value);

  final GlanceAlertsOpen _value;

  @override
  GlanceAlertsOpen critical(int? critical) => call(critical: critical);

  @override
  GlanceAlertsOpen warning(int? warning) => call(warning: warning);

  @override
  GlanceAlertsOpen info(int? info) => call(info: info);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GlanceAlertsOpen(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GlanceAlertsOpen(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GlanceAlertsOpen call({
    Object? critical = const $CopyWithPlaceholder(),
    Object? warning = const $CopyWithPlaceholder(),
    Object? info = const $CopyWithPlaceholder(),
  }) {
    return GlanceAlertsOpen(
      critical: critical == const $CopyWithPlaceholder()
          ? _value.critical
          // ignore: cast_nullable_to_non_nullable
          : critical as int?,
      warning: warning == const $CopyWithPlaceholder()
          ? _value.warning
          // ignore: cast_nullable_to_non_nullable
          : warning as int?,
      info: info == const $CopyWithPlaceholder()
          ? _value.info
          // ignore: cast_nullable_to_non_nullable
          : info as int?,
    );
  }
}

extension $GlanceAlertsOpenCopyWith on GlanceAlertsOpen {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGlanceAlertsOpen.copyWith(...)` or `instanceOfGlanceAlertsOpen.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GlanceAlertsOpenCWProxy get copyWith => _$GlanceAlertsOpenCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GlanceAlertsOpen _$GlanceAlertsOpenFromJson(Map<String, dynamic> json) =>
    $checkedCreate('GlanceAlertsOpen', json, ($checkedConvert) {
      final val = GlanceAlertsOpen(
        critical: $checkedConvert('critical', (v) => (v as num?)?.toInt()),
        warning: $checkedConvert('warning', (v) => (v as num?)?.toInt()),
        info: $checkedConvert('info', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$GlanceAlertsOpenToJson(GlanceAlertsOpen instance) =>
    <String, dynamic>{
      'critical': ?instance.critical,
      'warning': ?instance.warning,
      'info': ?instance.info,
    };
