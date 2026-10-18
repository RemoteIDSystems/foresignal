// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_note_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystNoteEventCWProxy {
  AnalystNoteEvent note(AnalystNote note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystNoteEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystNoteEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystNoteEvent call({AnalystNote note});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystNoteEvent.copyWith(...)` or call `instanceOfAnalystNoteEvent.copyWith.fieldName(value)` for a single field.
class _$AnalystNoteEventCWProxyImpl implements _$AnalystNoteEventCWProxy {
  const _$AnalystNoteEventCWProxyImpl(this._value);

  final AnalystNoteEvent _value;

  @override
  AnalystNoteEvent note(AnalystNote note) => call(note: note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystNoteEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystNoteEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystNoteEvent call({Object? note = const $CopyWithPlaceholder()}) {
    return AnalystNoteEvent(
      note: note == const $CopyWithPlaceholder() || note == null
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as AnalystNote,
    );
  }
}

extension $AnalystNoteEventCopyWith on AnalystNoteEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystNoteEvent.copyWith(...)` or `instanceOfAnalystNoteEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystNoteEventCWProxy get copyWith => _$AnalystNoteEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystNoteEvent _$AnalystNoteEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalystNoteEvent', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['note']);
      final val = AnalystNoteEvent(
        note: $checkedConvert(
          'note',
          (v) => AnalystNote.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AnalystNoteEventToJson(AnalystNoteEvent instance) =>
    <String, dynamic>{'note': instance.note.toJson()};
