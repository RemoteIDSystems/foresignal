// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_candidate_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorCandidatePageCWProxy {
  PrecursorCandidatePage items(List<PrecursorCandidate> items);

  PrecursorCandidatePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorCandidatePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorCandidatePage(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorCandidatePage call({
    List<PrecursorCandidate> items,
    String? nextCursor,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorCandidatePage.copyWith(...)` or call `instanceOfPrecursorCandidatePage.copyWith.fieldName(value)` for a single field.
class _$PrecursorCandidatePageCWProxyImpl
    implements _$PrecursorCandidatePageCWProxy {
  const _$PrecursorCandidatePageCWProxyImpl(this._value);

  final PrecursorCandidatePage _value;

  @override
  PrecursorCandidatePage items(List<PrecursorCandidate> items) =>
      call(items: items);

  @override
  PrecursorCandidatePage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorCandidatePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorCandidatePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorCandidatePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return PrecursorCandidatePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<PrecursorCandidate>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $PrecursorCandidatePageCopyWith on PrecursorCandidatePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorCandidatePage.copyWith(...)` or `instanceOfPrecursorCandidatePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorCandidatePageCWProxy get copyWith =>
      _$PrecursorCandidatePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorCandidatePage _$PrecursorCandidatePageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorCandidatePage', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items']);
  final val = PrecursorCandidatePage(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => PrecursorCandidate.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$PrecursorCandidatePageToJson(
  PrecursorCandidatePage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
