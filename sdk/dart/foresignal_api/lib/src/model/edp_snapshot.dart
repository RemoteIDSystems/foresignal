//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_snapshot.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpSnapshot {
  /// Returns a new [EdpSnapshot] instance.
  EdpSnapshot({

    required  this.id,

    required  this.stream,

     this.scope,

    required  this.page,

    required  this.final_,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'stream',
    required: true,
    includeIfNull: false,
  )


  final String stream;



      /// Value of the stream's snapshotScope property this snapshot covers; null = the whole stream
  @JsonKey(
    
    name: r'scope',
    required: false,
    includeIfNull: false,
  )


  final String? scope;



          // minimum: 1
  @JsonKey(
    
    name: r'page',
    required: true,
    includeIfNull: false,
  )


  final int page;



  @JsonKey(
    
    name: r'final',
    required: true,
    includeIfNull: false,
  )


  final bool final_;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpSnapshot &&
      other.id == id &&
      other.stream == stream &&
      other.scope == scope &&
      other.page == page &&
      other.final_ == final_;

    @override
    int get hashCode =>
        id.hashCode +
        stream.hashCode +
        (scope == null ? 0 : scope.hashCode) +
        page.hashCode +
        final_.hashCode;

  factory EdpSnapshot.fromJson(Map<String, dynamic> json) => _$EdpSnapshotFromJson(json);

  Map<String, dynamic> toJson() => _$EdpSnapshotToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

