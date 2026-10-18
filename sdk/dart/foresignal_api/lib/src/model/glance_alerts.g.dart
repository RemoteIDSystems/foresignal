// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'glance_alerts.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GlanceAlertsCWProxy {
  GlanceAlerts open(GlanceAlertsOpen? open);

  GlanceAlerts latest(List<GlanceAlertsLatestInner>? latest);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GlanceAlerts(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GlanceAlerts(...).copyWith(id: 12, name: "My name")
  /// ```
  GlanceAlerts call({
    GlanceAlertsOpen? open,
    List<GlanceAlertsLatestInner>? latest,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGlanceAlerts.copyWith(...)` or call `instanceOfGlanceAlerts.copyWith.fieldName(value)` for a single field.
class _$GlanceAlertsCWProxyImpl implements _$GlanceAlertsCWProxy {
  const _$GlanceAlertsCWProxyImpl(this._value);

  final GlanceAlerts _value;

  @override
  GlanceAlerts open(GlanceAlertsOpen? open) => call(open: open);

  @override
  GlanceAlerts latest(List<GlanceAlertsLatestInner>? latest) =>
      call(latest: latest);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GlanceAlerts(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GlanceAlerts(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GlanceAlerts call({
    Object? open = const $CopyWithPlaceholder(),
    Object? latest = const $CopyWithPlaceholder(),
  }) {
    return GlanceAlerts(
      open: open == const $CopyWithPlaceholder()
          ? _value.open
          // ignore: cast_nullable_to_non_nullable
          : open as GlanceAlertsOpen?,
      latest: latest == const $CopyWithPlaceholder()
          ? _value.latest
          // ignore: cast_nullable_to_non_nullable
          : latest as List<GlanceAlertsLatestInner>?,
    );
  }
}

extension $GlanceAlertsCopyWith on GlanceAlerts {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGlanceAlerts.copyWith(...)` or `instanceOfGlanceAlerts.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GlanceAlertsCWProxy get copyWith => _$GlanceAlertsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GlanceAlerts _$GlanceAlertsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('GlanceAlerts', json, ($checkedConvert) {
      final val = GlanceAlerts(
        open: $checkedConvert(
          'open',
          (v) => v == null
              ? null
              : GlanceAlertsOpen.fromJson(v as Map<String, dynamic>),
        ),
        latest: $checkedConvert(
          'latest',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) =>
                    GlanceAlertsLatestInner.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$GlanceAlertsToJson(GlanceAlerts instance) =>
    <String, dynamic>{
      'open': ?instance.open?.toJson(),
      'latest': ?instance.latest?.map((e) => e.toJson()).toList(),
    };
