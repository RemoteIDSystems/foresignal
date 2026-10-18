// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'problem_references.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProblemReferencesCWProxy {
  ProblemReferences rules(List<String>? rules);

  ProblemReferences views(List<String>? views);

  ProblemReferences briefings(List<String>? briefings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProblemReferences(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProblemReferences(...).copyWith(id: 12, name: "My name")
  /// ```
  ProblemReferences call({
    List<String>? rules,
    List<String>? views,
    List<String>? briefings,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProblemReferences.copyWith(...)` or call `instanceOfProblemReferences.copyWith.fieldName(value)` for a single field.
class _$ProblemReferencesCWProxyImpl implements _$ProblemReferencesCWProxy {
  const _$ProblemReferencesCWProxyImpl(this._value);

  final ProblemReferences _value;

  @override
  ProblemReferences rules(List<String>? rules) => call(rules: rules);

  @override
  ProblemReferences views(List<String>? views) => call(views: views);

  @override
  ProblemReferences briefings(List<String>? briefings) =>
      call(briefings: briefings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProblemReferences(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProblemReferences(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProblemReferences call({
    Object? rules = const $CopyWithPlaceholder(),
    Object? views = const $CopyWithPlaceholder(),
    Object? briefings = const $CopyWithPlaceholder(),
  }) {
    return ProblemReferences(
      rules: rules == const $CopyWithPlaceholder()
          ? _value.rules
          // ignore: cast_nullable_to_non_nullable
          : rules as List<String>?,
      views: views == const $CopyWithPlaceholder()
          ? _value.views
          // ignore: cast_nullable_to_non_nullable
          : views as List<String>?,
      briefings: briefings == const $CopyWithPlaceholder()
          ? _value.briefings
          // ignore: cast_nullable_to_non_nullable
          : briefings as List<String>?,
    );
  }
}

extension $ProblemReferencesCopyWith on ProblemReferences {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProblemReferences.copyWith(...)` or `instanceOfProblemReferences.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProblemReferencesCWProxy get copyWith =>
      _$ProblemReferencesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProblemReferences _$ProblemReferencesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ProblemReferences', json, ($checkedConvert) {
      final val = ProblemReferences(
        rules: $checkedConvert(
          'rules',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        views: $checkedConvert(
          'views',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        briefings: $checkedConvert(
          'briefings',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ProblemReferencesToJson(ProblemReferences instance) =>
    <String, dynamic>{
      'rules': ?instance.rules,
      'views': ?instance.views,
      'briefings': ?instance.briefings,
    };
