// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'investigation_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InvestigationPatchCWProxy {
  InvestigationPatch title(String? title);

  InvestigationPatch status(InvestigationPatchStatusEnum? status);

  InvestigationPatch summary(String? summary);

  InvestigationPatch tags(List<String>? tags);

  InvestigationPatch workspaceId(String? workspaceId);

  InvestigationPatch hypothesisSetIds(List<String>? hypothesisSetIds);

  InvestigationPatch questionIds(List<String>? questionIds);

  InvestigationPatch chronicleIds(List<String>? chronicleIds);

  InvestigationPatch reportIds(List<String>? reportIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InvestigationPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InvestigationPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  InvestigationPatch call({
    String? title,
    InvestigationPatchStatusEnum? status,
    String? summary,
    List<String>? tags,
    String? workspaceId,
    List<String>? hypothesisSetIds,
    List<String>? questionIds,
    List<String>? chronicleIds,
    List<String>? reportIds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInvestigationPatch.copyWith(...)` or call `instanceOfInvestigationPatch.copyWith.fieldName(value)` for a single field.
class _$InvestigationPatchCWProxyImpl implements _$InvestigationPatchCWProxy {
  const _$InvestigationPatchCWProxyImpl(this._value);

  final InvestigationPatch _value;

  @override
  InvestigationPatch title(String? title) => call(title: title);

  @override
  InvestigationPatch status(InvestigationPatchStatusEnum? status) =>
      call(status: status);

  @override
  InvestigationPatch summary(String? summary) => call(summary: summary);

  @override
  InvestigationPatch tags(List<String>? tags) => call(tags: tags);

  @override
  InvestigationPatch workspaceId(String? workspaceId) =>
      call(workspaceId: workspaceId);

  @override
  InvestigationPatch hypothesisSetIds(List<String>? hypothesisSetIds) =>
      call(hypothesisSetIds: hypothesisSetIds);

  @override
  InvestigationPatch questionIds(List<String>? questionIds) =>
      call(questionIds: questionIds);

  @override
  InvestigationPatch chronicleIds(List<String>? chronicleIds) =>
      call(chronicleIds: chronicleIds);

  @override
  InvestigationPatch reportIds(List<String>? reportIds) =>
      call(reportIds: reportIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InvestigationPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InvestigationPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InvestigationPatch call({
    Object? title = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? workspaceId = const $CopyWithPlaceholder(),
    Object? hypothesisSetIds = const $CopyWithPlaceholder(),
    Object? questionIds = const $CopyWithPlaceholder(),
    Object? chronicleIds = const $CopyWithPlaceholder(),
    Object? reportIds = const $CopyWithPlaceholder(),
  }) {
    return InvestigationPatch(
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as InvestigationPatchStatusEnum?,
      summary: summary == const $CopyWithPlaceholder()
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as String?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
      workspaceId: workspaceId == const $CopyWithPlaceholder()
          ? _value.workspaceId
          // ignore: cast_nullable_to_non_nullable
          : workspaceId as String?,
      hypothesisSetIds: hypothesisSetIds == const $CopyWithPlaceholder()
          ? _value.hypothesisSetIds
          // ignore: cast_nullable_to_non_nullable
          : hypothesisSetIds as List<String>?,
      questionIds: questionIds == const $CopyWithPlaceholder()
          ? _value.questionIds
          // ignore: cast_nullable_to_non_nullable
          : questionIds as List<String>?,
      chronicleIds: chronicleIds == const $CopyWithPlaceholder()
          ? _value.chronicleIds
          // ignore: cast_nullable_to_non_nullable
          : chronicleIds as List<String>?,
      reportIds: reportIds == const $CopyWithPlaceholder()
          ? _value.reportIds
          // ignore: cast_nullable_to_non_nullable
          : reportIds as List<String>?,
    );
  }
}

extension $InvestigationPatchCopyWith on InvestigationPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInvestigationPatch.copyWith(...)` or `instanceOfInvestigationPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InvestigationPatchCWProxy get copyWith =>
      _$InvestigationPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InvestigationPatch _$InvestigationPatchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('InvestigationPatch', json, ($checkedConvert) {
      final val = InvestigationPatch(
        title: $checkedConvert('title', (v) => v as String?),
        status: $checkedConvert(
          'status',
          (v) => $enumDecodeNullable(_$InvestigationPatchStatusEnumEnumMap, v),
        ),
        summary: $checkedConvert('summary', (v) => v as String?),
        tags: $checkedConvert(
          'tags',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        workspaceId: $checkedConvert('workspaceId', (v) => v as String?),
        hypothesisSetIds: $checkedConvert(
          'hypothesisSetIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        questionIds: $checkedConvert(
          'questionIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        chronicleIds: $checkedConvert(
          'chronicleIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        reportIds: $checkedConvert(
          'reportIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$InvestigationPatchToJson(InvestigationPatch instance) =>
    <String, dynamic>{
      'title': ?instance.title,
      'status': ?_$InvestigationPatchStatusEnumEnumMap[instance.status],
      'summary': ?instance.summary,
      'tags': ?instance.tags,
      'workspaceId': ?instance.workspaceId,
      'hypothesisSetIds': ?instance.hypothesisSetIds,
      'questionIds': ?instance.questionIds,
      'chronicleIds': ?instance.chronicleIds,
      'reportIds': ?instance.reportIds,
    };

const _$InvestigationPatchStatusEnumEnumMap = {
  InvestigationPatchStatusEnum.open: 'open',
  InvestigationPatchStatusEnum.active: 'active',
  InvestigationPatchStatusEnum.closed: 'closed',
  InvestigationPatchStatusEnum.archived: 'archived',
};
