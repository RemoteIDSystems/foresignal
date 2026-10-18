// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exercise_run_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExerciseRunPageCWProxy {
  ExerciseRunPage items(List<ExerciseRun> items);

  ExerciseRunPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExerciseRunPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExerciseRunPage(...).copyWith(id: 12, name: "My name")
  /// ```
  ExerciseRunPage call({List<ExerciseRun> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExerciseRunPage.copyWith(...)` or call `instanceOfExerciseRunPage.copyWith.fieldName(value)` for a single field.
class _$ExerciseRunPageCWProxyImpl implements _$ExerciseRunPageCWProxy {
  const _$ExerciseRunPageCWProxyImpl(this._value);

  final ExerciseRunPage _value;

  @override
  ExerciseRunPage items(List<ExerciseRun> items) => call(items: items);

  @override
  ExerciseRunPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExerciseRunPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExerciseRunPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExerciseRunPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ExerciseRunPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ExerciseRun>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ExerciseRunPageCopyWith on ExerciseRunPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExerciseRunPage.copyWith(...)` or `instanceOfExerciseRunPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExerciseRunPageCWProxy get copyWith => _$ExerciseRunPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExerciseRunPage _$ExerciseRunPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ExerciseRunPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = ExerciseRunPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => ExerciseRun.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ExerciseRunPageToJson(ExerciseRunPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
