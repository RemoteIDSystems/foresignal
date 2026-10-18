// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'investigation_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InvestigationInputCWProxy {
  InvestigationInput title(String title);

  InvestigationInput summary(String? summary);

  InvestigationInput tags(List<String>? tags);

  InvestigationInput status(InvestigationInputStatusEnum? status);

  InvestigationInput visibility(Visibility? visibility);

  InvestigationInput tlp(Tlp? tlp);

  InvestigationInput workspaceId(String? workspaceId);

  InvestigationInput hypothesisSetIds(List<String>? hypothesisSetIds);

  InvestigationInput questionIds(List<String>? questionIds);

  InvestigationInput chronicleIds(List<String>? chronicleIds);

  InvestigationInput reportIds(List<String>? reportIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InvestigationInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InvestigationInput(...).copyWith(id: 12, name: "My name")
  /// ```
  InvestigationInput call({
    String title,
    String? summary,
    List<String>? tags,
    InvestigationInputStatusEnum? status,
    Visibility? visibility,
    Tlp? tlp,
    String? workspaceId,
    List<String>? hypothesisSetIds,
    List<String>? questionIds,
    List<String>? chronicleIds,
    List<String>? reportIds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInvestigationInput.copyWith(...)` or call `instanceOfInvestigationInput.copyWith.fieldName(value)` for a single field.
class _$InvestigationInputCWProxyImpl implements _$InvestigationInputCWProxy {
  const _$InvestigationInputCWProxyImpl(this._value);

  final InvestigationInput _value;

  @override
  InvestigationInput title(String title) => call(title: title);

  @override
  InvestigationInput summary(String? summary) => call(summary: summary);

  @override
  InvestigationInput tags(List<String>? tags) => call(tags: tags);

  @override
  InvestigationInput status(InvestigationInputStatusEnum? status) =>
      call(status: status);

  @override
  InvestigationInput visibility(Visibility? visibility) =>
      call(visibility: visibility);

  @override
  InvestigationInput tlp(Tlp? tlp) => call(tlp: tlp);

  @override
  InvestigationInput workspaceId(String? workspaceId) =>
      call(workspaceId: workspaceId);

  @override
  InvestigationInput hypothesisSetIds(List<String>? hypothesisSetIds) =>
      call(hypothesisSetIds: hypothesisSetIds);

  @override
  InvestigationInput questionIds(List<String>? questionIds) =>
      call(questionIds: questionIds);

  @override
  InvestigationInput chronicleIds(List<String>? chronicleIds) =>
      call(chronicleIds: chronicleIds);

  @override
  InvestigationInput reportIds(List<String>? reportIds) =>
      call(reportIds: reportIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InvestigationInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InvestigationInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InvestigationInput call({
    Object? title = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? visibility = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? workspaceId = const $CopyWithPlaceholder(),
    Object? hypothesisSetIds = const $CopyWithPlaceholder(),
    Object? questionIds = const $CopyWithPlaceholder(),
    Object? chronicleIds = const $CopyWithPlaceholder(),
    Object? reportIds = const $CopyWithPlaceholder(),
  }) {
    return InvestigationInput(
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      summary: summary == const $CopyWithPlaceholder()
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as String?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as InvestigationInputStatusEnum?,
      visibility: visibility == const $CopyWithPlaceholder()
          ? _value.visibility
          // ignore: cast_nullable_to_non_nullable
          : visibility as Visibility?,
      tlp: tlp == const $CopyWithPlaceholder()
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp?,
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

extension $InvestigationInputCopyWith on InvestigationInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInvestigationInput.copyWith(...)` or `instanceOfInvestigationInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InvestigationInputCWProxy get copyWith =>
      _$InvestigationInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InvestigationInput _$InvestigationInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('InvestigationInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['title']);
      final val = InvestigationInput(
        title: $checkedConvert('title', (v) => v as String),
        summary: $checkedConvert('summary', (v) => v as String?),
        tags: $checkedConvert(
          'tags',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        status: $checkedConvert(
          'status',
          (v) => $enumDecodeNullable(_$InvestigationInputStatusEnumEnumMap, v),
        ),
        visibility: $checkedConvert(
          'visibility',
          (v) => $enumDecodeNullable(_$VisibilityEnumMap, v),
        ),
        tlp: $checkedConvert(
          'tlp',
          (v) => $enumDecodeNullable(_$TlpEnumMap, v),
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

Map<String, dynamic> _$InvestigationInputToJson(InvestigationInput instance) =>
    <String, dynamic>{
      'title': instance.title,
      'summary': ?instance.summary,
      'tags': ?instance.tags,
      'status': ?_$InvestigationInputStatusEnumEnumMap[instance.status],
      'visibility': ?_$VisibilityEnumMap[instance.visibility],
      'tlp': ?_$TlpEnumMap[instance.tlp],
      'workspaceId': ?instance.workspaceId,
      'hypothesisSetIds': ?instance.hypothesisSetIds,
      'questionIds': ?instance.questionIds,
      'chronicleIds': ?instance.chronicleIds,
      'reportIds': ?instance.reportIds,
    };

const _$InvestigationInputStatusEnumEnumMap = {
  InvestigationInputStatusEnum.open: 'open',
  InvestigationInputStatusEnum.active: 'active',
  InvestigationInputStatusEnum.closed: 'closed',
  InvestigationInputStatusEnum.archived: 'archived',
};

const _$VisibilityEnumMap = {
  Visibility.private: 'private',
  Visibility.shared: 'shared',
  Visibility.instance: 'instance',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};
