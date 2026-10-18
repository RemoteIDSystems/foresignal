// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'glance_activity.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GlanceActivityCWProxy {
  GlanceActivity versionsLastHour(int? versionsLastHour);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GlanceActivity(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GlanceActivity(...).copyWith(id: 12, name: "My name")
  /// ```
  GlanceActivity call({int? versionsLastHour});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGlanceActivity.copyWith(...)` or call `instanceOfGlanceActivity.copyWith.fieldName(value)` for a single field.
class _$GlanceActivityCWProxyImpl implements _$GlanceActivityCWProxy {
  const _$GlanceActivityCWProxyImpl(this._value);

  final GlanceActivity _value;

  @override
  GlanceActivity versionsLastHour(int? versionsLastHour) =>
      call(versionsLastHour: versionsLastHour);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GlanceActivity(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GlanceActivity(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GlanceActivity call({
    Object? versionsLastHour = const $CopyWithPlaceholder(),
  }) {
    return GlanceActivity(
      versionsLastHour: versionsLastHour == const $CopyWithPlaceholder()
          ? _value.versionsLastHour
          // ignore: cast_nullable_to_non_nullable
          : versionsLastHour as int?,
    );
  }
}

extension $GlanceActivityCopyWith on GlanceActivity {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGlanceActivity.copyWith(...)` or `instanceOfGlanceActivity.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GlanceActivityCWProxy get copyWith => _$GlanceActivityCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GlanceActivity _$GlanceActivityFromJson(Map<String, dynamic> json) =>
    $checkedCreate('GlanceActivity', json, ($checkedConvert) {
      final val = GlanceActivity(
        versionsLastHour: $checkedConvert(
          'versionsLastHour',
          (v) => (v as num?)?.toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$GlanceActivityToJson(GlanceActivity instance) =>
    <String, dynamic>{'versionsLastHour': ?instance.versionsLastHour};
