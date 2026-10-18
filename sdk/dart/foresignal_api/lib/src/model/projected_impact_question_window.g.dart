// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'projected_impact_question_window.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProjectedImpactQuestionWindowCWProxy {
  ProjectedImpactQuestionWindow start(DateTime? start);

  ProjectedImpactQuestionWindow end(DateTime? end);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProjectedImpactQuestionWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProjectedImpactQuestionWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  ProjectedImpactQuestionWindow call({DateTime? start, DateTime? end});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProjectedImpactQuestionWindow.copyWith(...)` or call `instanceOfProjectedImpactQuestionWindow.copyWith.fieldName(value)` for a single field.
class _$ProjectedImpactQuestionWindowCWProxyImpl
    implements _$ProjectedImpactQuestionWindowCWProxy {
  const _$ProjectedImpactQuestionWindowCWProxyImpl(this._value);

  final ProjectedImpactQuestionWindow _value;

  @override
  ProjectedImpactQuestionWindow start(DateTime? start) => call(start: start);

  @override
  ProjectedImpactQuestionWindow end(DateTime? end) => call(end: end);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProjectedImpactQuestionWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProjectedImpactQuestionWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProjectedImpactQuestionWindow call({
    Object? start = const $CopyWithPlaceholder(),
    Object? end = const $CopyWithPlaceholder(),
  }) {
    return ProjectedImpactQuestionWindow(
      start: start == const $CopyWithPlaceholder()
          ? _value.start
          // ignore: cast_nullable_to_non_nullable
          : start as DateTime?,
      end: end == const $CopyWithPlaceholder()
          ? _value.end
          // ignore: cast_nullable_to_non_nullable
          : end as DateTime?,
    );
  }
}

extension $ProjectedImpactQuestionWindowCopyWith
    on ProjectedImpactQuestionWindow {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProjectedImpactQuestionWindow.copyWith(...)` or `instanceOfProjectedImpactQuestionWindow.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProjectedImpactQuestionWindowCWProxy get copyWith =>
      _$ProjectedImpactQuestionWindowCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProjectedImpactQuestionWindow _$ProjectedImpactQuestionWindowFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ProjectedImpactQuestionWindow', json, ($checkedConvert) {
  final val = ProjectedImpactQuestionWindow(
    start: $checkedConvert(
      'start',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    end: $checkedConvert(
      'end',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$ProjectedImpactQuestionWindowToJson(
  ProjectedImpactQuestionWindow instance,
) => <String, dynamic>{
  'start': ?instance.start?.toIso8601String(),
  'end': ?instance.end?.toIso8601String(),
};
