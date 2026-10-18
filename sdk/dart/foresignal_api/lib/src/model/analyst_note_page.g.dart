// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_note_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystNotePageCWProxy {
  AnalystNotePage items(List<AnalystNote> items);

  AnalystNotePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystNotePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystNotePage(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystNotePage call({List<AnalystNote> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystNotePage.copyWith(...)` or call `instanceOfAnalystNotePage.copyWith.fieldName(value)` for a single field.
class _$AnalystNotePageCWProxyImpl implements _$AnalystNotePageCWProxy {
  const _$AnalystNotePageCWProxyImpl(this._value);

  final AnalystNotePage _value;

  @override
  AnalystNotePage items(List<AnalystNote> items) => call(items: items);

  @override
  AnalystNotePage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystNotePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystNotePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystNotePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return AnalystNotePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<AnalystNote>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $AnalystNotePageCopyWith on AnalystNotePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystNotePage.copyWith(...)` or `instanceOfAnalystNotePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystNotePageCWProxy get copyWith => _$AnalystNotePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystNotePage _$AnalystNotePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalystNotePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = AnalystNotePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => AnalystNote.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AnalystNotePageToJson(AnalystNotePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
