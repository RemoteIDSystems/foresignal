// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'history_frame.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HistoryFrameCWProxy {
  HistoryFrame t(DateTime t);

  HistoryFrame upserts(List<FrameItem> upserts);

  HistoryFrame removals(List<HistoryFrameRemovalsInner> removals);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HistoryFrame(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HistoryFrame(...).copyWith(id: 12, name: "My name")
  /// ```
  HistoryFrame call({
    DateTime t,
    List<FrameItem> upserts,
    List<HistoryFrameRemovalsInner> removals,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHistoryFrame.copyWith(...)` or call `instanceOfHistoryFrame.copyWith.fieldName(value)` for a single field.
class _$HistoryFrameCWProxyImpl implements _$HistoryFrameCWProxy {
  const _$HistoryFrameCWProxyImpl(this._value);

  final HistoryFrame _value;

  @override
  HistoryFrame t(DateTime t) => call(t: t);

  @override
  HistoryFrame upserts(List<FrameItem> upserts) => call(upserts: upserts);

  @override
  HistoryFrame removals(List<HistoryFrameRemovalsInner> removals) =>
      call(removals: removals);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HistoryFrame(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HistoryFrame(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HistoryFrame call({
    Object? t = const $CopyWithPlaceholder(),
    Object? upserts = const $CopyWithPlaceholder(),
    Object? removals = const $CopyWithPlaceholder(),
  }) {
    return HistoryFrame(
      t: t == const $CopyWithPlaceholder() || t == null
          ? _value.t
          // ignore: cast_nullable_to_non_nullable
          : t as DateTime,
      upserts: upserts == const $CopyWithPlaceholder() || upserts == null
          ? _value.upserts
          // ignore: cast_nullable_to_non_nullable
          : upserts as List<FrameItem>,
      removals: removals == const $CopyWithPlaceholder() || removals == null
          ? _value.removals
          // ignore: cast_nullable_to_non_nullable
          : removals as List<HistoryFrameRemovalsInner>,
    );
  }
}

extension $HistoryFrameCopyWith on HistoryFrame {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHistoryFrame.copyWith(...)` or `instanceOfHistoryFrame.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HistoryFrameCWProxy get copyWith => _$HistoryFrameCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HistoryFrame _$HistoryFrameFromJson(Map<String, dynamic> json) =>
    $checkedCreate('HistoryFrame', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['t', 'upserts', 'removals']);
      final val = HistoryFrame(
        t: $checkedConvert('t', (v) => DateTime.parse(v as String)),
        upserts: $checkedConvert(
          'upserts',
          (v) => (v as List<dynamic>)
              .map((e) => FrameItem.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        removals: $checkedConvert(
          'removals',
          (v) => (v as List<dynamic>)
              .map(
                (e) => HistoryFrameRemovalsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$HistoryFrameToJson(HistoryFrame instance) =>
    <String, dynamic>{
      't': instance.t.toIso8601String(),
      'upserts': instance.upserts.map((e) => e.toJson()).toList(),
      'removals': instance.removals.map((e) => e.toJson()).toList(),
    };
