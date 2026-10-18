// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'investigation.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InvestigationCWProxy {
  Investigation id(String id);

  Investigation displayId(String displayId);

  Investigation title(String title);

  Investigation status(InvestigationStatusEnum status);

  Investigation summary(String? summary);

  Investigation tags(List<String>? tags);

  Investigation itemCount(int? itemCount);

  Investigation items(List<InvestigationItem>? items);

  Investigation createdBy(String createdBy);

  Investigation createdAt(DateTime createdAt);

  Investigation updatedAt(DateTime updatedAt);

  Investigation owner(PrincipalSummary? owner);

  Investigation visibility(Visibility? visibility);

  Investigation tlp(Tlp? tlp);

  Investigation members(List<ObjectAclEntry>? members);

  Investigation workspaceId(String? workspaceId);

  Investigation hypothesisSetIds(List<String>? hypothesisSetIds);

  Investigation questionIds(List<String>? questionIds);

  Investigation chronicleIds(List<String>? chronicleIds);

  Investigation reportIds(List<String>? reportIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Investigation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Investigation(...).copyWith(id: 12, name: "My name")
  /// ```
  Investigation call({
    String id,
    String displayId,
    String title,
    InvestigationStatusEnum status,
    String? summary,
    List<String>? tags,
    int? itemCount,
    List<InvestigationItem>? items,
    String createdBy,
    DateTime createdAt,
    DateTime updatedAt,
    PrincipalSummary? owner,
    Visibility? visibility,
    Tlp? tlp,
    List<ObjectAclEntry>? members,
    String? workspaceId,
    List<String>? hypothesisSetIds,
    List<String>? questionIds,
    List<String>? chronicleIds,
    List<String>? reportIds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInvestigation.copyWith(...)` or call `instanceOfInvestigation.copyWith.fieldName(value)` for a single field.
class _$InvestigationCWProxyImpl implements _$InvestigationCWProxy {
  const _$InvestigationCWProxyImpl(this._value);

  final Investigation _value;

  @override
  Investigation id(String id) => call(id: id);

  @override
  Investigation displayId(String displayId) => call(displayId: displayId);

  @override
  Investigation title(String title) => call(title: title);

  @override
  Investigation status(InvestigationStatusEnum status) => call(status: status);

  @override
  Investigation summary(String? summary) => call(summary: summary);

  @override
  Investigation tags(List<String>? tags) => call(tags: tags);

  @override
  Investigation itemCount(int? itemCount) => call(itemCount: itemCount);

  @override
  Investigation items(List<InvestigationItem>? items) => call(items: items);

  @override
  Investigation createdBy(String createdBy) => call(createdBy: createdBy);

  @override
  Investigation createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Investigation updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  @override
  Investigation owner(PrincipalSummary? owner) => call(owner: owner);

  @override
  Investigation visibility(Visibility? visibility) =>
      call(visibility: visibility);

  @override
  Investigation tlp(Tlp? tlp) => call(tlp: tlp);

  @override
  Investigation members(List<ObjectAclEntry>? members) =>
      call(members: members);

  @override
  Investigation workspaceId(String? workspaceId) =>
      call(workspaceId: workspaceId);

  @override
  Investigation hypothesisSetIds(List<String>? hypothesisSetIds) =>
      call(hypothesisSetIds: hypothesisSetIds);

  @override
  Investigation questionIds(List<String>? questionIds) =>
      call(questionIds: questionIds);

  @override
  Investigation chronicleIds(List<String>? chronicleIds) =>
      call(chronicleIds: chronicleIds);

  @override
  Investigation reportIds(List<String>? reportIds) =>
      call(reportIds: reportIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Investigation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Investigation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Investigation call({
    Object? id = const $CopyWithPlaceholder(),
    Object? displayId = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? itemCount = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
    Object? createdBy = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? owner = const $CopyWithPlaceholder(),
    Object? visibility = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? members = const $CopyWithPlaceholder(),
    Object? workspaceId = const $CopyWithPlaceholder(),
    Object? hypothesisSetIds = const $CopyWithPlaceholder(),
    Object? questionIds = const $CopyWithPlaceholder(),
    Object? chronicleIds = const $CopyWithPlaceholder(),
    Object? reportIds = const $CopyWithPlaceholder(),
  }) {
    return Investigation(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      displayId: displayId == const $CopyWithPlaceholder() || displayId == null
          ? _value.displayId
          // ignore: cast_nullable_to_non_nullable
          : displayId as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as InvestigationStatusEnum,
      summary: summary == const $CopyWithPlaceholder()
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as String?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
      itemCount: itemCount == const $CopyWithPlaceholder()
          ? _value.itemCount
          // ignore: cast_nullable_to_non_nullable
          : itemCount as int?,
      items: items == const $CopyWithPlaceholder()
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<InvestigationItem>?,
      createdBy: createdBy == const $CopyWithPlaceholder() || createdBy == null
          ? _value.createdBy
          // ignore: cast_nullable_to_non_nullable
          : createdBy as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
      owner: owner == const $CopyWithPlaceholder()
          ? _value.owner
          // ignore: cast_nullable_to_non_nullable
          : owner as PrincipalSummary?,
      visibility: visibility == const $CopyWithPlaceholder()
          ? _value.visibility
          // ignore: cast_nullable_to_non_nullable
          : visibility as Visibility?,
      tlp: tlp == const $CopyWithPlaceholder()
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp?,
      members: members == const $CopyWithPlaceholder()
          ? _value.members
          // ignore: cast_nullable_to_non_nullable
          : members as List<ObjectAclEntry>?,
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

extension $InvestigationCopyWith on Investigation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInvestigation.copyWith(...)` or `instanceOfInvestigation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InvestigationCWProxy get copyWith => _$InvestigationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Investigation _$InvestigationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Investigation', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'displayId',
      'title',
      'status',
      'createdBy',
      'createdAt',
      'updatedAt',
    ],
  );
  final val = Investigation(
    id: $checkedConvert('id', (v) => v as String),
    displayId: $checkedConvert('displayId', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$InvestigationStatusEnumEnumMap, v),
    ),
    summary: $checkedConvert('summary', (v) => v as String?),
    tags: $checkedConvert(
      'tags',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    itemCount: $checkedConvert('itemCount', (v) => (v as num?)?.toInt()),
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>?)
          ?.map((e) => InvestigationItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    createdBy: $checkedConvert('createdBy', (v) => v as String),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert('updatedAt', (v) => DateTime.parse(v as String)),
    owner: $checkedConvert(
      'owner',
      (v) => v == null
          ? null
          : PrincipalSummary.fromJson(v as Map<String, dynamic>),
    ),
    visibility: $checkedConvert(
      'visibility',
      (v) => $enumDecodeNullable(_$VisibilityEnumMap, v),
    ),
    tlp: $checkedConvert('tlp', (v) => $enumDecodeNullable(_$TlpEnumMap, v)),
    members: $checkedConvert(
      'members',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ObjectAclEntry.fromJson(e as Map<String, dynamic>))
          .toList(),
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

Map<String, dynamic> _$InvestigationToJson(Investigation instance) =>
    <String, dynamic>{
      'id': instance.id,
      'displayId': instance.displayId,
      'title': instance.title,
      'status': _$InvestigationStatusEnumEnumMap[instance.status]!,
      'summary': ?instance.summary,
      'tags': ?instance.tags,
      'itemCount': ?instance.itemCount,
      'items': ?instance.items?.map((e) => e.toJson()).toList(),
      'createdBy': instance.createdBy,
      'createdAt': instance.createdAt.toIso8601String(),
      'updatedAt': instance.updatedAt.toIso8601String(),
      'owner': ?instance.owner?.toJson(),
      'visibility': ?_$VisibilityEnumMap[instance.visibility],
      'tlp': ?_$TlpEnumMap[instance.tlp],
      'members': ?instance.members?.map((e) => e.toJson()).toList(),
      'workspaceId': ?instance.workspaceId,
      'hypothesisSetIds': ?instance.hypothesisSetIds,
      'questionIds': ?instance.questionIds,
      'chronicleIds': ?instance.chronicleIds,
      'reportIds': ?instance.reportIds,
    };

const _$InvestigationStatusEnumEnumMap = {
  InvestigationStatusEnum.open: 'open',
  InvestigationStatusEnum.closed: 'closed',
  InvestigationStatusEnum.active: 'active',
  InvestigationStatusEnum.archived: 'archived',
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
