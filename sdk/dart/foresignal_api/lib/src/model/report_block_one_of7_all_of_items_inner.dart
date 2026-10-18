//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_block_one_of7_all_of_items_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportBlockOneOf7AllOfItemsInner {
  /// Returns a new [ReportBlockOneOf7AllOfItemsInner] instance.
  ReportBlockOneOf7AllOfItemsInner({

    required  this.evidenceItemId,

     this.title,

    required  this.grade,

     this.snapshot,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'evidenceItemId',
    required: true,
    includeIfNull: false,
  )


  final String evidenceItemId;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'grade',
    required: true,
    includeIfNull: false,
  )


  final String grade;



  @JsonKey(
    
    name: r'snapshot',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? snapshot;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportBlockOneOf7AllOfItemsInner &&
      other.evidenceItemId == evidenceItemId &&
      other.title == title &&
      other.grade == grade &&
      other.snapshot == snapshot;

    @override
    int get hashCode =>
        evidenceItemId.hashCode +
        title.hashCode +
        grade.hashCode +
        snapshot.hashCode;

  factory ReportBlockOneOf7AllOfItemsInner.fromJson(Map<String, dynamic> json) => _$ReportBlockOneOf7AllOfItemsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ReportBlockOneOf7AllOfItemsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

