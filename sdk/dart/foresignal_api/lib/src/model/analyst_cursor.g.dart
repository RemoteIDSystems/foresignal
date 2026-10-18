// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_cursor.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystCursorCWProxy {
  AnalystCursor seq(int? seq);

  AnalystCursor alertId(int? alertId);

  AnalystCursor eventUpdatedAt(DateTime? eventUpdatedAt);

  AnalystCursor anomalyId(int? anomalyId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystCursor(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystCursor(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystCursor call({
    int? seq,
    int? alertId,
    DateTime? eventUpdatedAt,
    int? anomalyId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystCursor.copyWith(...)` or call `instanceOfAnalystCursor.copyWith.fieldName(value)` for a single field.
class _$AnalystCursorCWProxyImpl implements _$AnalystCursorCWProxy {
  const _$AnalystCursorCWProxyImpl(this._value);

  final AnalystCursor _value;

  @override
  AnalystCursor seq(int? seq) => call(seq: seq);

  @override
  AnalystCursor alertId(int? alertId) => call(alertId: alertId);

  @override
  AnalystCursor eventUpdatedAt(DateTime? eventUpdatedAt) =>
      call(eventUpdatedAt: eventUpdatedAt);

  @override
  AnalystCursor anomalyId(int? anomalyId) => call(anomalyId: anomalyId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystCursor(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystCursor(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystCursor call({
    Object? seq = const $CopyWithPlaceholder(),
    Object? alertId = const $CopyWithPlaceholder(),
    Object? eventUpdatedAt = const $CopyWithPlaceholder(),
    Object? anomalyId = const $CopyWithPlaceholder(),
  }) {
    return AnalystCursor(
      seq: seq == const $CopyWithPlaceholder()
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int?,
      alertId: alertId == const $CopyWithPlaceholder()
          ? _value.alertId
          // ignore: cast_nullable_to_non_nullable
          : alertId as int?,
      eventUpdatedAt: eventUpdatedAt == const $CopyWithPlaceholder()
          ? _value.eventUpdatedAt
          // ignore: cast_nullable_to_non_nullable
          : eventUpdatedAt as DateTime?,
      anomalyId: anomalyId == const $CopyWithPlaceholder()
          ? _value.anomalyId
          // ignore: cast_nullable_to_non_nullable
          : anomalyId as int?,
    );
  }
}

extension $AnalystCursorCopyWith on AnalystCursor {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystCursor.copyWith(...)` or `instanceOfAnalystCursor.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystCursorCWProxy get copyWith => _$AnalystCursorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystCursor _$AnalystCursorFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalystCursor', json, ($checkedConvert) {
      final val = AnalystCursor(
        seq: $checkedConvert('seq', (v) => (v as num?)?.toInt()),
        alertId: $checkedConvert('alertId', (v) => (v as num?)?.toInt()),
        eventUpdatedAt: $checkedConvert(
          'eventUpdatedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        anomalyId: $checkedConvert('anomalyId', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$AnalystCursorToJson(AnalystCursor instance) =>
    <String, dynamic>{
      'seq': ?instance.seq,
      'alertId': ?instance.alertId,
      'eventUpdatedAt': ?instance.eventUpdatedAt?.toIso8601String(),
      'anomalyId': ?instance.anomalyId,
    };
